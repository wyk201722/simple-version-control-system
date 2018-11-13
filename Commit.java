package gitlet;
import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.io.FileInputStream;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
/**
 * Created by IvesYao on 2017/7/15.
 */
public class Commit implements Serializable {
    private String repo = ".gitlet/";
    private String hashId;
    private Commit parent;
    private String commitPath;
    private String commitMsg;
    private Stage currStage;
    private ArrayList<File> stagedFiles = new ArrayList<>();
    private ArrayList<String> filesNames = new ArrayList<String>();
    private ArrayList<byte[]> byteFile = new ArrayList<>();
    private ArrayList<String> hashID = new ArrayList<>();
    private String stageRepo = ".gitlet/StagingArea/";
    private Date commitTime;
    private String dateString = "";
    private Branch currBranch;
    private ArrayList<String> unFile = new ArrayList<>();
    private HashMap<String, String> mapFromNameToHash = new HashMap<>();
    private HashMap<File, String> mapFromFileToHash = new HashMap<>();
    private HashMap<File, byte[]> mapFromFileToByte = new HashMap<>();
    public Branch getCurrBranch() {
        return currBranch;
    }
    private ArrayList<String> toPass = new ArrayList<>();
    private ArrayList<String> realRemove = new ArrayList<>();
    public void setMapFromNameToHash(HashMap<String, String> mapFromNameToHash) {
        this.mapFromNameToHash = mapFromNameToHash;
    }

    public HashMap<String, String> getMapFromNameToHash() {
        return mapFromNameToHash;
    }

    public void setCurrBranch(Branch currBranch) {
        this.currBranch = currBranch;
    }

    public ArrayList<File> getStagedFiles() {
        return stagedFiles;
    }

    public ArrayList<String> getFilesNames() {
        return filesNames;
    }

    public ArrayList<String> getRealRemove() {
        return realRemove;
    }

    public void setRealRemove(ArrayList<String> realRemove) {
        this.realRemove = realRemove;
    }

    public ArrayList<byte[]> getByteFile() {
        return byteFile;
    }

    public ArrayList<String> getHashID() {
        return hashID;
    }

    public String getCommitMsg() {
        return commitMsg;
    }

    public Date getCommitTime() {
        return commitTime;
    }

    public String getHashId() {
        return hashId;
    }

    public Commit getParent() {
        return parent;
    }

    public String getCommitParh() {
        return repo + getHashId();
    }

    public void setHashId(String hashaId) {
        this.hashId = hashaId;
    }

    public void setParent(Commit parent) {
        this.parent = parent;
    }

    public void setCommitParh(String fileParh) {
        commitPath = fileParh;
    }

    public ArrayList<String> getUnFile() {
        return unFile;
    }

    public Commit(String commitMsg, Commit parent) {
        this.unFile = new ArrayList<>();
        this.realRemove = new ArrayList<>();
        commitTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.dateString = formatter.format(commitTime);
        if (parent != null) {
            this.parent = parent;
        }
        this.commitMsg = commitMsg;
        this.currStage = loadStage();
        //this.filesNames = currStage.getFiles();
        if (parent != null) {
            for (String a : this.getParent().getFilesNames()) {
                if (!parent.getUnFile().contains(a) && (!currStage.getFiles().contains(a))) {
                    this.filesNames.add(a);
                    File f = new File(this.getParent().getCommitParh()
                            + "/" + this.getParent().getMapFromNameToHash().get(a));
                    byte[] temp = Utils.readContents(f);
                    byteFile.add(temp);
                    String smallId = Utils.sha1(temp);
                    hashID.add(smallId);
                    stagedFiles.add(f);
                    mapFromNameToHash.put(a, smallId);
                    mapFromFileToHash.put(f, smallId);
                    mapFromFileToByte.put(f, temp);
                }
            }
        }
        for (String a : currStage.getFiles()) {
            File f = new File(a);
            stagedFiles.add(f);
            filesNames.add(a);
            byte[] temp = Utils.readContents(f);
            byteFile.add(temp);
            String smallId = Utils.sha1(temp);
            hashID.add(smallId);
            mapFromNameToHash.put(a, smallId);
            mapFromFileToHash.put(f, smallId);
            mapFromFileToByte.put(f, temp);
        }
        String toSha = "";
        for (String a :hashID) {
            toSha += a;
        }
        toSha += dateString;
        String temp = Utils.sha1(toSha);
        this.setHashId(temp);
        commitPath = repo + this.getHashId();
//        this.setHashId(Utils.sha1(currStage.getFiles()));
        if (parent == null) {
            File iniFile = new File(this.getCommitParh());
            iniFile.mkdir();
            this.seriCommit();
        }

    }
    public void generateFiles() {
        File commitFileFolder = new File(commitPath);
        commitFileFolder.mkdir();
        for (File i:this.stagedFiles) {
            String tempFileName = mapFromFileToHash.get(i);
            String tempFileName1 = this.commitPath + "/" + tempFileName;
            File newFile = new File(tempFileName1);
            Utils.writeContents(newFile, mapFromFileToByte.get(i));
        }
    }

    public void seriCommit() {
        Object obj = this;
        File outFile = new File(this.commitPath + "/" + "seriCommit");
        try {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(obj);
            out.close();
        } catch (IOException excp) {
            System.out.println("Wrong");
        }
    }

    public Stage loadStage() {
        Object obj = null;
        File inFile = new File(stageRepo);
        if (inFile.exists()) {
            try {
                ObjectInputStream inp =
                        new ObjectInputStream(new FileInputStream(inFile));
                obj = (Object) inp.readObject();
                inp.close();
            } catch (IOException | ClassNotFoundException excp) {
                obj = null;
            }
        }
        if (obj != null) {
            Stage curStage = (Stage) obj;
            return curStage;
        } else {
            Stage curStage = new Stage();
            return curStage;
        }
    }
    public void commit() {
        currBranch.setRemovedName(new ArrayList<>());
        generateFiles();

        seriCommit();
    }
    public String toString() {
        String result = "";
        result += "===" + "\n" + "Commit" + " "
                + this.getHashId() + "\n" + this.dateString + "\n" + this.getCommitMsg();
        return result;
    }
    public static Commit loadCommit(String repo) {

        Commit obj;
        File inFile = new File(repo);
        if (inFile.exists()) {
            try {

                ObjectInputStream inp =
                        new ObjectInputStream(new FileInputStream(inFile));
                obj = (Commit) inp.readObject();
                inp.close();
            } catch (IOException | ClassNotFoundException excp) {
                obj = null;
            }
            return (Commit) obj;
        } else {
            return null;
        }
    }
}
