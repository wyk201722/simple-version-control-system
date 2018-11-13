package gitlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by IvesYao on 2017/7/16.
 */
public class Branch implements Serializable {
    private String branchName;
    private Commit headPointer;
    private Stage currentStage;
    private String repo = ".gitlet/StagingArea/";
    private boolean successFlag = true;
    private ArrayList<String> removedName = new ArrayList<>();
    public Branch(String branchName, Commit headPointer) {
        this.branchName = branchName;
        this.headPointer = headPointer;
        this.currentStage = this.loadStagingFile();
    }

    public ArrayList<String> getRemovedName() {
        return removedName;
    }

    public void setRemovedName(ArrayList<String> removedName) {
        this.removedName = removedName;
    }

    public Branch(ArrayList<String> removedName) {
        this.removedName = removedName;
    }

    public boolean isSuccessFlag() {
        return successFlag;
    }

    public String getBranchName() {
        return branchName;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Commit getHeadPointer() {
        return headPointer;
    }

    public void setHeadPointer(Commit headPointer) {
        this.headPointer = headPointer;
    }

    public void add(String fileName) {
        File text = new File(fileName);
        if (text.exists()) {
            if (!(Utils.sha1(Utils.readContents(text))
                    .equals(this.getHeadPointer().getMapFromNameToHash().get(fileName)))) {
                this.currentStage.getFiles().add(fileName);
                this.currentStage.generateStagingFile();
            }
        } else {
            System.out.println("File does not exist.");
        }
        if (this.getHeadPointer().getUnFile().contains(fileName)) {
            this.getHeadPointer().getUnFile().remove(fileName);
            this.getHeadPointer().getRealRemove().remove(fileName);
        }
    }

    public Stage loadStagingFile() {
        Object obj;
        File inFile = new File(repo);
        if (inFile.exists()) {
            try {
                ObjectInputStream inp =
                        new ObjectInputStream(new FileInputStream(inFile));
                obj = (Object) inp.readObject();
                inp.close();
            } catch (IOException | ClassNotFoundException excp) {
                obj = null;
            }
            return (Stage) obj;
        } else {
            return new Stage();
        }
    }

    public void commit(String msg) {
        currentStage = loadStagingFile();
        if (!currentStage.getFiles().isEmpty() || !headPointer.getUnFile().isEmpty()) {
            Commit newCommit = new Commit(msg, headPointer);
            this.setHeadPointer(newCommit);
            newCommit.setCurrBranch(this);
            newCommit.commit();
            Stage newStage = new Stage();
            newStage.generateStagingFile();
            this.setCurrentStage(newStage);
            successFlag = true;
        } else {
            System.out.print("No changes added to the commit.");
            successFlag = false;
        }
    }

    public void remove(String fileName) {
        Boolean flag = false;
        for (String i : Utils.plainFilenamesIn(System.getProperty("user.dir"))) {
            if (i.equals(fileName)) {
                if (this.getHeadPointer().getFilesNames().contains(i)) {
                    File testFile = new File(i);
                    byte[] testByte = Utils.readContents(testFile);
                    String testHash = Utils.sha1(testByte);
                    if (testHash.equals(this.getHeadPointer().getMapFromNameToHash().get(i))) {
                        flag = true;
                        File delFile = new File(fileName);
                        delFile.delete();
                        removedName.add(fileName);
                        headPointer.getUnFile().add(fileName);
                        headPointer.getRealRemove().add(fileName);
                        headPointer.seriCommit();
                    }
                }
            }
        }
        if (currentStage.getFiles().contains(fileName)) {
            currentStage.remove(fileName);
            currentStage.generateStagingFile();
            flag = true;
        }

        if (headPointer.getFilesNames().contains(fileName)) {
            if (!headPointer.getRealRemove().contains(fileName)) {
                headPointer.getRealRemove().add(fileName);
                headPointer.getUnFile().add(fileName);
            }
            flag = true;
        }


        if (!flag) {
            System.out.print("No reason to remove the file.");
        }
    }
}
