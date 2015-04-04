package com.pastekit.tools.kitten;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.search.*;
import com.google.appengine.tools.remoteapi.LoginException;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.googlecode.gql4j.GqlQuery;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Command(name = "kitten", description = "pastekit command line utility")
public class Kitten {

    @Inject
    public HelpOption helpOption;

    @Option(name = {"-A", "--appId"}, description = "GAE Application ID")
    public String appId;

    @Option(name = {"-U", "--user"}, description = "GAE Username")
    public String username;

    @Option(name = {"-P", "--pass"}, description = "GAE Password")
    public String password;

    @Option(name = {"-Q", "--query"}, description = "Query string")
    public String query;

    @Option(name = {"--update"}, description = "Update values for result data set")
    public String update;

    private static final String EMPTY_NAMESPACE = "";

    public static void main( String[] args ) throws IOException {

        Kitten kitten = SingleCommand.singleCommand(Kitten.class).parse(args);
        if (kitten.helpOption.showHelpIfRequested()) {
            return;
        }
        kitten.run();

    }

    public void run() throws IOException{
        System.out.println("Trying to connect to app: " + appId);
        RemoteApiOptions options = new RemoteApiOptions()
                .server( appId + ".appspot.com", 443)
                .credentials(username, password);
        RemoteApiInstaller installer = new RemoteApiInstaller();
        try {
            System.out.println("Executing query: " + query);
            installer.install(options);
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            GqlQuery gql = new GqlQuery(query);
            Iterable<Entity> result = ds.prepare(gql.query()).asIterable(gql.fetchOptions());
            if(update != null && !update.isEmpty()){
                TransactionOptions ops = TransactionOptions.Builder.withXG(true);
                Transaction txn = ds.beginTransaction(ops);
                try {
                    for(Entity e : result){
                        Map<String,Object> updates = marshalMap(splitToMap(update));
                        Iterator<Map.Entry<String,Object>> it = updates.entrySet().iterator();
                        while(it.hasNext()){
                            Map.Entry<String,Object> entry = it.next();
                            String key = entry.getKey();
                            Object newVal = entry.getValue();

                            Object oldVal = e.getProperties().get(key);
                            if(newVal.getClass().equals(oldVal.getClass())){
                                System.out.println("Updating Entity key="
                                        + KeyFactory.keyToString(e.getKey())
                                        + " with property=" + key + " value=" + newVal);
                                e.setProperty(key, newVal);
                                ds.put(e);
                            } else {
                                throw new RuntimeException("Incompatible assignment of " + oldVal.getClass().getName()
                                        + " to " + oldVal.getClass().getName());
                            }
                        }
                    }
                    txn.commit();
                } catch (Exception e){
                    System.out.println("Error occurred rolling back...");
                    e.printStackTrace();
                } finally {
                    if (txn.isActive()) {
                        txn.rollback();
                    }
                }
            } else {
                int i =  0;
                for(Entity e : result){
                    i++;
                    System.out.println("************************************************");
                    System.out.println("ENTITY " + i);
                    System.out.println("************************************************");
                    System.out.println(KeyFactory.keyToString(e.getKey()));
                    Map<String,Object> props = e.getProperties();
                    Gson gson = new Gson();
                    String json = gson.toJson(props);
                    System.out.println(json);
                    System.out.println("************************************************");

                }
            }
            installer.install(options);
        } catch (Exception e){
            if(e instanceof LoginException){
                System.out.println("Unable to connect wrong appId/username and/or password");
            }
        } finally {
            installer.uninstall();
        }
    }

    private Map<String, Object> marshalMap(Map<String, String> in) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Iterator<Map.Entry<String,String>> it = in.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,String> entry = it.next();
            String key = entry.getKey();
            String val = entry.getValue();
            if(val.startsWith("\"") && val.endsWith("\"")){
                result.put(key, val);
            } else if(val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")){
                result.put(key, val.equalsIgnoreCase("true") ? true : false);
            } else if(val.equalsIgnoreCase("null")) {
                result.put(key, null);
            } else if(isNumeric(val)) {
                try {
                    Long l = Long.valueOf(val);
                    result.put(key, l);
                } catch (NumberFormatException e){
                    throw new RuntimeException("Invalid number: " + val);
                }
            } else {
                throw new RuntimeException("Update parameter(s) contained unsupported type");
            }
        }
        return result;
    }

    private Map<String, String> splitToMap(String in) {
        return Splitter.on(",").withKeyValueSeparator("=").split(in);
    }

    public static boolean isNumeric(String string){
        Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d)?$");
        return pattern.matcher(string).matches();
    }

    private void printEntity(Entity e){

    }

}
