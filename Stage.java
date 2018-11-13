package gitlet;


import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * Created by IvesYao on 2017/7/15.
 */
public class Stage implements Serializable {
    private ArrayList<String> files = new ArrayList<String>();

    private String repo = ".gitlet/StagingArea/";

    public ArrayList<String> getFiles() {
        return files;
    }

    public void add(String fileName) {

    }

    public void remove(String fileName) {
        files.remove(fileName);
    }



    public void generateStagingFile() {
        Object obj = this;
        File outFile = new File(repo);
        try {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(obj);
            out.close();
        } catch (IOException excp) {
            System.out.println("Ives is stupid");
        }
    }

}
