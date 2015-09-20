/**
 * Your application code goes here
 */
package userclasses;

import com.codename1.analytics.AnalyticsService;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.processing.Result;
import generated.StateMachineBase;
import com.codename1.ui.*;
import com.codename1.ui.events.*;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.Layout;
import com.codename1.ui.util.Resources;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Your name here
 */
public class StateMachine extends StateMachineBase {

    String roar;

    public StateMachine(String resFile) {
        super(resFile);
        // do not modify, write code in initVars and initialize class members there,
        // the constructor might be invoked too late due to race conditions that might occur
    }

    /**
     * this method should be used to initialize variables instead of the
     * constructor/class scope to avoid race conditions
     */
    protected void initVars(Resources res) {
        
        AnalyticsService.init("UA-67803686-1", "irrelevant");
        AnalyticsService.setAppsMode(true);
        
    }

    @Override
    protected void onMain_ButtonAction(Component c, ActionEvent event) {

        Hashtable infoToBeSent = new Hashtable();
        infoToBeSent.put("roar", roar);
        infoToBeSent.put("author", "seinecle");

        final String infoInString = Result.fromContent(infoToBeSent).toString();

        String firebase = "https://roar.firebaseIO.com/listofroars.json";

        ConnectionRequest request = new ConnectionRequest() {
            @Override
            protected void buildRequestBody(OutputStream os) throws IOException {
                os.write(infoInString.getBytes("UTF-8"));
            }

        };
        request.setUrl(firebase);
        request.setPost(true);
        request.setHttpMethod("POST");
        request.setContentType("application/json");

        NetworkManager.getInstance().addToQueueAndWait(request);

    }

    @Override
    protected void onMain_TextAreaAction(Component c, ActionEvent event) {
        roar = findTextArea().getText();
        if (roar == null) {
            roar = "we did not get a roar from you";
        }

    }

    @Override
    protected void onWall_ButtonAction(Component c, ActionEvent event) {

        try {
            String roars = "https://roar.firebaseIO.com/listofroars.json";

            //if we want to retrieve only the latest 10 roars posted
            //String roars = "https://roar.firebaseIO.com/listofroars.json" + "?" + "orderBy=\"$key\"" + "&" + "limitToLast=10";
            ConnectionRequest request = new ConnectionRequest();
            request.setUrl(roars);
            request.setPost(false);
            request.setHttpMethod("GET");
            request.setContentType("application/json");

            NetworkManager.getInstance().addToQueueAndWait(request);

            ByteArrayInputStream allRoarsInBytes = new ByteArrayInputStream(request.getResponseData());
            String responseInString = Util.readToString(allRoarsInBytes, "UTF-8");

            JSONObject allRoarsInJsonFormat = new JSONObject(responseInString);
            JSONArray listOfRoarIds = allRoarsInJsonFormat.names();

            Form wallScreen = c.getComponentForm();

            Container myContainerForAllRoars = new Container();
            Layout myLayout = new BoxLayout(BoxLayout.Y_AXIS);
            myContainerForAllRoars.setLayout(myLayout);

            Integer counterOfRoars = 0;

            while (counterOfRoars < allRoarsInJsonFormat.length()) {
                String idOfOneRoar = listOfRoarIds.getString(counterOfRoars);
                JSONObject oneRoarInJsonFormat = (JSONObject) allRoarsInJsonFormat.get(idOfOneRoar);

                Container myRoarContainer = new Container();

                String author = oneRoarInJsonFormat.getString("author");
                String roarText = oneRoarInJsonFormat.getString("roar");

                Label myLabelForAuthor = new Label(author);
                Label myLabelForRoar = new Label(roarText);

                myRoarContainer.addComponent(myLabelForAuthor);
                myRoarContainer.addComponent(myLabelForRoar);

                myContainerForAllRoars.addComponent(myRoarContainer);

                counterOfRoars = counterOfRoars + 1;

            }
            wallScreen.addComponent(wallScreen.getComponentCount(), myContainerForAllRoars);
            wallScreen.revalidate();
        } catch (IOException ex) {
        } catch (JSONException ex) {

        }

    }

    @Override
    protected void onCreateUserName() {
        String userName;
        userName = Preferences.get("username", "");
        if (userName != null) {
            showForm("Main", null);
            AnalyticsService.visit("Main", "UserName");
            
        }
    }

    @Override
    protected void onUserName_ButtonAction(Component c, ActionEvent event) {
        String userName = findTextField().getText();
        if (userName == null || userName.length() == 0) {

        } else {
            Preferences.set("username", userName);
            showForm("Main", null);
            AnalyticsService.visit("Main", "UserName");
        }
    }
}
