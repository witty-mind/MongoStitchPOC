package com.example.debasishkumardas.demoapp;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchAuth;
import com.mongodb.stitch.android.core.auth.StitchAuthListener;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.android.services.mongodb.remote.SyncFindIterable;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ChangeEventListener;
import com.mongodb.stitch.core.services.mongodb.remote.sync.DefaultSyncConflictResolvers;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ErrorListener;
import com.mongodb.stitch.core.services.mongodb.remote.sync.internal.ChangeEvent;

import org.bson.BsonValue;
import org.bson.Document;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    private StitchAppClient _client;
    private RemoteMongoClient _mongoClient;
    private RemoteMongoCollection _remoteCollection;
    String getId = null;

    EditText etUserName, etPassword;
    Button btnLogin;
    TextView tvCreateAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUserName = (EditText) findViewById(R.id.etUserId);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvCreateAccount = (TextView) findViewById(R.id.tvCreateAccount);

        setUpMongoDbClient();

        //createNewUser();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etUserName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                getAuth(userName, password);
            }
        });

        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpScreen.class));
            }
        });

    }


    void setUpMongoDbClient(){
        this._client = Stitch.initializeAppClient("demosyncapplication-sjmhh");
        //this._client.getAuth().addAuthListener(new MyAuthListener(this));

        _mongoClient = this._client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        _remoteCollection = _mongoClient.getDatabase("Troy").getCollection("troy-collection");

        _remoteCollection.sync().configure(
                DefaultSyncConflictResolvers.remoteWins(),
                new MyUpdateListener(),
                new MyErrorListener());
    }

    private class MyUpdateListener implements ChangeEventListener<Document> {
        @Override
        public void onEvent(final BsonValue documentId, final ChangeEvent<Document> event) {

            // Is this change coming from local or remote?
            if (event.hasUncommittedWrites()) { //change initiated on the device
                Log.d("STITCH", "Local change to document " + documentId);
                Toast.makeText(MainActivity.this, "Local change to document", Toast.LENGTH_SHORT).show();
            } else { //remote change
                Log.d("STITCH", "Remote change to document " + documentId);
                Toast.makeText(MainActivity.this, "Remote change to document", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class MyErrorListener implements ErrorListener {
        @Override
        public void onError(BsonValue documentId, Exception error) {
            Log.e("Stitch", error.getLocalizedMessage());

            Set<BsonValue> docsThatNeedToBeFixed = _remoteCollection.sync().getPausedDocumentIds();
            for (BsonValue doc_id : docsThatNeedToBeFixed) {
                // Add your logic to inform the user.
                // When errors have been resolved, call
                _remoteCollection.sync().resumeSyncForDocument(doc_id);
        }
        }
    }

    void getAuth(String userName, String password){
        this._client.getAuth().loginWithCredential(new UserPasswordCredential(userName, password)).addOnCompleteListener(
                new OnCompleteListener<StitchUser>() {

                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            Log.d("myApp", String.format(
                                    "logged in as user %s with provider %s",
                                    task.getResult().getId(),
                                    task.getResult().getLoggedInProviderType()));
                            Toast.makeText(MainActivity.this, "Successfully Logged In.", Toast.LENGTH_SHORT).show();

                            getId = task.getResult().getId();

                            if(!TextUtils.isEmpty(getId)){
                                addData();
                            }else{
                                Toast.makeText(MainActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                            }
                            Log.d("Id", getId);
                        } else {
                            String getException = task.getException().getMessage().toString();
                            Toast.makeText(MainActivity.this, getException, Toast.LENGTH_SHORT).show();
                            resetAllValues();
                        }
                    }
                });
    }

    void resetAllValues(){
        etUserName.setText(null);
        etPassword.setText(null);
    }

    /*void createNewUser(){
        UserPasswordAuthProviderClient emailPassClient = Stitch.getDefaultAppClient().getAuth().getProviderClient(
                UserPasswordAuthProviderClient.factory);

        emailPassClient.registerWithEmail("debasish@gmail.com", "1234567890")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull final Task<Void> task) {
                                               if (task.isSuccessful()) {
                                                   Log.d("stitch", "Successfully sent account confirmation email");
                                               } else {
                                                   Log.e("stitch", "Error registering new user:", task.getException());
                                               }
                                           }
                                       }
                );
    }*/


    void linkUser(){
        // Get the currently logged-in (anonymous) user from Stitch.
        final StitchAuth auth = Stitch.getDefaultAppClient().getAuth();
        final StitchUser user = auth.getUser();

        // Create and link a new account with the given username and password.
        user.linkWithCredential(new UserPasswordCredential("debasish@gmail.com", "1234567890"))
                .addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull Task<StitchUser> task) {
                        // User is now linked
                        getId = task.getResult().getId();
                    }
                });
    }




    void addData() {
        Document document = new Document("name", "Tea Trail")
                .append("owner_id", getId)
                .append("contact", new Document("phone", "420-345-2345")
                        .append("email", "cafecoffeeday@example.com")
                        .append("location",Arrays.asList(-73.92502, 40.8279556)))
                .append("stars", 3)
                .append("review", 4)
                .append("customers", Arrays.asList("Maven", "David", "Peter", "Parker"))
                .append("categories", Arrays.asList("Bakery", "Coffee", "Pastries"));

        final Task<RemoteInsertOneResult> res = _remoteCollection.sync().insertOne(document);


        res.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull final Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                    getData();
                } else {
                    Log.e("Demo Tag", "Error adding item", task.getException());
                }
            }
        });

    }

    void getData(){
        /*_client = Stitch.getDefaultAppClient();
        _mongoClient = _client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");

        // Read data from a collection
        final Document query = Document.parse("{ 'owner_id': { getId } }");
        RemoteFindIterable cursor = _remoteCollection.find(query).limit(100);
        final ArrayList<Document> findResults = new ArrayList<>();
        cursor.into(findResults).addOnCompleteListener(new onCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) { *//* do something... *//* }
                else { *//* handle the exception... *//* }
            }
        })*/

       /* Document filter = new Document("owner_id", getId);
        SyncFindIterable cursor = _remoteCollection.sync().find(filter).limit(100);
        ArrayList<Document> documents = new ArrayList<>();
        int arraySize = documents.size();
        Log.d("Size", String.valueOf(arraySize));*/

        Document filter = new Document("owner_id", _client.getAuth().getUser().getId());
        SyncFindIterable cursor = _remoteCollection.sync().find(filter).limit(100);
        final ArrayList<Document> documents = new ArrayList<>();
        cursor.into(documents).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
               if(task.isSuccessful()){
                   Toast.makeText(MainActivity.this, "The Transaction was Successful", Toast.LENGTH_SHORT).show();

                   String values = documents.toString();
                   Log.d("Updated Values", values);
               }
            }
        });
    }


}
