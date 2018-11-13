package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by IvesYao on 2017/7/16.
 */
public class CommitTree implements Serializable {
    private HashMap<String, String> commitMap = new HashMap<String, String>();
    private Branch currBranch;
    private HashMap<String, Branch> branchHashMap = new HashMap<String, Branch>();
    private String treeRepo = ".gitlet/treeRepo.ser";
    private Branch changedBranch;

    private HashMap<String, String> shortMap = new HashMap<>();

    public void setCommitMap(HashMap<String, String> commitMap) {
        this.commitMap = commitMap;
    }

    public HashMap<String, Branch> getBranchHashMap() {
        return branchHashMap;
    }

    public void setBranchHashMap(HashMap<String, Branch> branchHashMap) {
        this.branchHashMap = branchHashMap;
    }

    public String getTreeRepo() {
        return treeRepo;
    }

    public HashMap<String, String> getCommitMap() {
        return commitMap;
    }

    public Branch getCurrBranch() {
        return currBranch;
    }

    public HashMap<String, String> getShortMap() {
        return shortMap;
    }

    public void setCurrBranch(Branch currBranch) {
        this.currBranch = currBranch;
    }

    public static CommitTree commitTreeInit() {
        CommitTree commitTree = new CommitTree();
        String commitMessage = "initial commit";
        String branchName = "master";
        Commit initCommit = new Commit(commitMessage, null);
        Branch initBranch = new Branch(branchName, initCommit);
        commitTree.setCurrBranch(initBranch);
        commitTree.branchHashMap.put(branchName, initBranch);
        commitTree.commitMap.put(initCommit.getHashId(), initCommit.getCommitParh());
        return commitTree;
    }
    public void add(String fileName) {
        currBranch.add(fileName);
    }
    public void commit(String msg) {
        currBranch.commit(msg);
        if (currBranch.isSuccessFlag()) {

            Commit temp = currBranch.getHeadPointer();
            commitMap.put(temp.getHashId(), temp.getCommitParh());
        }
    }

    public void logPrint() {
        Commit commitPointer = currBranch.getHeadPointer();
        while (commitPointer != null) {
            System.out.println(commitPointer.toString());
            System.out.println();
            commitPointer = commitPointer.getParent();
        }
    }
    public void globalPrint() {
        for (String i : this.getCommitMap().keySet()) {
            String repo = this.getCommitMap().get(i);
            repo += "/seriCommit";
            Commit newCommit = Commit.loadCommit(repo);
            System.out.println(newCommit.toString());
            System.out.println();
        }
    }

    public void remove(String fileName) {
        currBranch.remove(fileName);
    }

    public void printStatus() {
        System.out.println("=== Branches ===");
        System.out.print("*");
        System.out.println(currBranch.getBranchName());
        for (String i : branchHashMap.keySet()) {
            if (!i.equals(currBranch.getBranchName())) {
                System.out.println(i);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String i :currBranch.getCurrentStage().getFiles()) {
            System.out.println(i);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String i : currBranch.getHeadPointer().getRealRemove()) {
            System.out.println(i);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();

    }

    public void branch(String branchName) {
        if (!this.getBranchHashMap().keySet().contains(branchName)) {
            Branch newBranch = new Branch(branchName, currBranch.getHeadPointer());
            this.getBranchHashMap().put(branchName, newBranch);
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    public void rmBranch(String branchName) {
        if (branchName.equals(this.getCurrBranch().getBranchName())) {
            System.out.println("Cannot remove the current branch.");
        } else if (!this.getBranchHashMap().keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            this.getBranchHashMap().remove(branchName);
        }
    }

    public void checkOutFromCommit(String token) {
        Commit cm = this.getCurrBranch().getHeadPointer();
        if (cm.getFilesNames().contains(token)) {
            String repo  = cm.getCommitParh() + "/" + cm.getMapFromNameToHash().get(token);
            File oldFile = new File(repo);
            byte[] info = Utils.readContents(oldFile);
            File newFile = new File(token);
            Utils.writeContents(newFile, info);
        } else {
            System.out.println("File does not exist in that commit.");
        }

    }
    public void checkOutFromFolder(String token1, String token2) {
        Commit cm = null;
        if (!this.getCommitMap().containsKey(token1)) {
            System.out.println("No commit with that id exists.");
        } else {
            cm = Commit.loadCommit(this.getCommitMap().get(token1) + "/" + "seriCommit");
            if (cm.getFilesNames().contains(token2)) {
                String repo  = cm.getCommitParh() + "/" + cm.getMapFromNameToHash().get(token2);
                File oldFile = new File(repo);
                byte[] info = Utils.readContents(oldFile);
                File newFile = new File(token2);
                Utils.writeContents(newFile, info);
            } else {
                System.out.println("File does not exist in that commit.");
            }

        }
    }

    public void checkOutBranch(String token) {

        if (token.equals(currBranch.getBranchName())) {
            System.out.print("No need to checkout the current branch.");
        } else if (!this.getBranchHashMap().containsKey(token)) {
            System.out.println("No such branch exists.");
        } else {
            ArrayList<String> toTest = new ArrayList<>();
            ArrayList<String> toTest1 = new ArrayList<>();
            ArrayList<String> toTest2 = new ArrayList<>();
            changedBranch = this.getBranchHashMap().get(token);
            Commit changedPointer = changedBranch.getHeadPointer();
            for (String i : changedBranch.getHeadPointer().getFilesNames()) {
                toTest.add(i);
                toTest1.add(i);
            }

            for (String i : toTest) {
                if (changedBranch.getHeadPointer().getUnFile().contains(i)) {
                    toTest1.remove(i);
                }
            }
            ArrayList<String> folderNames = new ArrayList<>();
            for (String i:Utils.plainFilenamesIn(System.getProperty("user.dir"))) {
                folderNames.add(i);
            }
            for (String i : toTest1) {
                if (folderNames.contains(i)) {
                    toTest2.add(i);
                }
            }
            boolean flag = true;
            for (String i : toTest2) {
                if (!currBranch.getHeadPointer().getFilesNames().contains(i)) {
                    flag = false;
                }
            }
            if (!flag) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
            } else {
                for (String i : currBranch.getHeadPointer().getFilesNames()) {
                    if (!changedBranch.getHeadPointer().getFilesNames().contains(i)
                            || changedBranch.getHeadPointer().getUnFile().contains(i)) {
                        File toDel = new File(i);
                        toDel.delete();
                    }
                }
                for (String i : changedBranch.getHeadPointer().getFilesNames()) {
                    if (!changedBranch.getHeadPointer().getUnFile().contains(i)) {
                        File originFile = new File(changedBranch.getHeadPointer().getCommitParh()
                                + "/"
                                + changedBranch.getHeadPointer().getMapFromNameToHash().get(i));
                        byte[] temp = Utils.readContents(originFile);
                        File newFile = new File(i);
                        Utils.writeContents(newFile, temp);
                    }
                }
                this.currBranch.setCurrentStage(new Stage());
                changedBranch.setCurrentStage(new Stage());
                this.setCurrBranch(changedBranch);
                this.currBranch.getHeadPointer().setRealRemove(new ArrayList<>());
                this.currBranch.setHeadPointer(changedPointer);
                Main.saveCommitTree();
            }
        }
    }
    public void checkOutShort(String msg, String fileName) {
        this.setCommitIdFromShort();
        String id = "";
        if (this.getShortMap().containsKey(msg)) {
            id = this.getShortMap().get(msg);
            checkOutFromFolder(id, fileName);
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    public void setCommitIdFromShort() {
        for (String i : this.getCommitMap().keySet()) {
            String s = new String();
            s = i.substring(0, 8);
            this.getShortMap().put(s, i);
        }

    }

    public void reset(String commitMsg) {
        if (!this.getCommitMap().containsKey(commitMsg)) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit cm = Commit.loadCommit(".gitlet/" + commitMsg + "/seriCommit");
            ArrayList<String> toTest = new ArrayList<>();
            ArrayList<String> toTest1 = new ArrayList<>();
            ArrayList<String> toTest2 = new ArrayList<>();

            for (String i : cm.getFilesNames()) {
                toTest.add(i);
                toTest1.add(i);
            }

            for (String i : toTest) {
                if (cm.getUnFile().contains(i)) {
                    toTest1.remove(i);
                }
            }
            ArrayList<String> folderNames = new ArrayList<>();
            for (String i:Utils.plainFilenamesIn(System.getProperty("user.dir"))) {
                folderNames.add(i);
            }
            for (String i : toTest1) {
                if (folderNames.contains(i)) {
                    toTest2.add(i);
                }
            }
            boolean flag = true;
            for (String i : toTest2) {
                if (!currBranch.getHeadPointer().getFilesNames().contains(i)) {
                    flag = false;
                }
            }
            if (!flag) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
            } else {

                for (String i : currBranch.getHeadPointer().getFilesNames()) {
                    if (!cm.getFilesNames().contains(i) || cm.getUnFile().contains(i)) {
                        File toDel = new File(i);
                        toDel.delete();
                    }
                }
                for (String i : cm.getFilesNames()) {
                    if (!cm.getUnFile().contains(i)) {
                        File originFile = new File(cm.getCommitParh()
                                + "/" + cm.getMapFromNameToHash().get(i));
                        byte[] temp = Utils.readContents(originFile);
                        File newFile = new File(i);
                        Utils.writeContents(newFile, temp);
                    }
                }
                this.currBranch.setCurrentStage(new Stage());
                cm.getRealRemove().clear();
                this.currBranch.getCurrentStage().generateStagingFile();
                this.setCurrBranch(cm.getCurrBranch());
                this.getBranchHashMap().get(cm.getCurrBranch().getBranchName()).setHeadPointer(cm);
//                this.getCurrBranch().getHeadPointer().getUnFile().clear();
//                this.getCurrBranch().getHeadPointer().getRealRemove().clear();
                currBranch.setHeadPointer(cm);
                Main.saveCommitTree();
            }
        }
    }

    public void printFind(String msg) {
        boolean flag = false;
        ArrayList<String> result = new ArrayList<>();
        for (String i : this.getCommitMap().keySet()) {
            Commit cm = Commit.loadCommit(".gitlet/" + i + "/" + "seriCommit");
            if (cm.getCommitMsg().equals(msg)) {
                flag = true;
                result.add(i);
            }
        }
        if (flag) {
            for (String i : result) {
                System.out.println(i);
            }
        } else {
            System.out.println("Found no commit with that message.");
        }
    }

    public void merge(String branchName) {
        if ((!currBranch.loadStagingFile().getFiles().isEmpty())
                || (!currBranch.getHeadPointer().getRealRemove().isEmpty())) {
            System.out.println("You have uncommitted changes");
        } else if (!this.getBranchHashMap().keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchName.equals(this.getCurrBranch().getBranchName())) {
            System.out.println("Cannot merge a branch with itself.");
        } else {
            Commit givenBranchCommit = this.getBranchHashMap().get(branchName).getHeadPointer();
            Commit currBranchCommit = this.getCurrBranch().getHeadPointer();
            Commit splitPoint = mergeHelp(givenBranchCommit, currBranchCommit);
            ArrayList<String> toTest = new ArrayList<>();
            ArrayList<String> toTest1 = new ArrayList<>();
            ArrayList<String> toTest2 = new ArrayList<>();
            ArrayList<String> toTest3 = new ArrayList<>();
            iniCheck(givenBranchCommit, toTest, toTest1, toTest2);
            boolean flag = true;
            for (String i : toTest2) {
                if (!currBranchCommit.getFilesNames().contains(i)) {
                    flag = false;
                }
            }
            if (!flag) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
            } else {
                if (splitPoint.getHashId().equals(givenBranchCommit.getHashId())) {
                    System.out.println("Given branch is an ancestor of the current branch");
                } else if (splitPoint.getHashId().equals(currBranchCommit.getHashId())) {
                    checkOutBranch(branchName);
                    System.out.println("Current branch fast-forwarded.");
                } else {
                    ArrayList<String> conflicts1 = new ArrayList<>();
                    ArrayList<String> conflicts2 = new ArrayList<>();
                    boolean flag1;
                    flag1 = flagGet(givenBranchCommit,
                            splitPoint, currBranchCommit, conflicts1, conflicts2);
                    for (String i: conflicts1) {
                        print2(givenBranchCommit, currBranchCommit, i);
                    }
                    for (String i: conflicts2) {
                        print1(givenBranchCommit, currBranchCommit, i);
                    }
                    if (!flag1) {
                        System.out.println("Encountered a merge conflict.");
                    }
                    ArrayList<String> filesShouldOverDirect = new ArrayList<>();
                    ArrayList<String> filesOnlyInGiven = new ArrayList<>();
                    ArrayList<String> filesToRemove = new ArrayList<>();
                    check1(splitPoint, currBranchCommit, givenBranchCommit,
                            filesShouldOverDirect, filesToRemove, filesOnlyInGiven);
                    addRemove(filesShouldOverDirect, givenBranchCommit,
                            flag1, filesOnlyInGiven, filesToRemove);
                    if (flag1) {
                        this.commit("Merged "
                                + getCurrBranch().getBranchName() + " with " + branchName + ".");
                        Main.saveCommitTree();
                    }
                }
            }
        }
    }

    public void print1(Commit givenBranchCommit, Commit currBranchCommit, String i) {
        byte[] byteForFileInGiven = null;
        byte[] byteForFileInCurr = null;
        if (givenBranchCommit.getFilesNames().contains(i)) {
            byteForFileInGiven = Utils.readContents(new File(givenBranchCommit.getCommitParh() + "/"
                    + givenBranchCommit.getMapFromNameToHash().get(i)));
            String ele1 = "<<<<<<< HEAD\r\n";
            String ele2 = "=======\r\n";
            String ele3 = ">>>>>>>";
            String changeLine = "\r\n";
            byte[] newByte = ele1.getBytes();
            newByte = unitByteArray(newByte, changeLine.getBytes());
            newByte = unitByteArray(newByte, ele2.getBytes());
            newByte = unitByteArray(newByte, byteForFileInGiven);
            newByte = unitByteArray(newByte, ele3.getBytes());
            newByte = unitByteArray(newByte, changeLine.getBytes());
            File geneFile = new File(i);
            Utils.writeContents(geneFile, newByte);
        }
        if (currBranchCommit.getFilesNames().contains(i)) {
            byteForFileInCurr = Utils.readContents(new File(currBranchCommit.getCommitParh() + "/"
                    + currBranchCommit.getMapFromNameToHash().get(i)));
            String ele1 = "<<<<<<< HEAD\r\n";
            String ele2 = "=======\r\n";
            String ele3 = ">>>>>>>";
            String changeLine = "\r\n";
            byte[] newByte = unitByteArray(ele1.getBytes(), byteForFileInCurr);
            newByte = unitByteArray(newByte, ele2.getBytes());
            newByte = unitByteArray(newByte, ele3.getBytes());
            newByte = unitByteArray(newByte, changeLine.getBytes());
            File geneFile = new File(i);
            Utils.writeContents(geneFile, newByte);
        }
    }
    public void print2(Commit givenBranchCommit, Commit currBranchCommit, String i) {
        byte[] byteForFileInGiven = Utils.readContents(new File(givenBranchCommit
                .getCommitParh() + "/"
                + givenBranchCommit.getMapFromNameToHash().get(i)));
        byte[] byteForFileInCurr = Utils.readContents(new File(currBranchCommit
                .getCommitParh() + "/"
                + currBranchCommit.getMapFromNameToHash().get(i)));
        String ele1 = "<<<<<<< HEAD\r\n";
        String ele2 = "=======\r\n";
        String ele3 = ">>>>>>>";
        String changeLine = "\r\n";
        byte[] newByte = unitByteArray(ele1.getBytes(), byteForFileInCurr);
//                            newByte = unitByteArray(newByte,changeLine.getBytes());
        newByte = unitByteArray(newByte, ele2.getBytes());
        newByte = unitByteArray(newByte, byteForFileInGiven);
        newByte = unitByteArray(newByte, ele3.getBytes());
        newByte = unitByteArray(newByte, changeLine.getBytes());
        File geneFile = new File(i);
        Utils.writeContents(geneFile, newByte);

    }
    public void check1(Commit splitPoint, Commit currBranchCommit,
                       Commit givenBranchCommit, ArrayList<String> filesShouldOverDirect,
                       ArrayList<String> filesToRemove, ArrayList<String> filesOnlyInGiven) {
        for (String i :splitPoint.getFilesNames()) {
            if (currBranchCommit.getFilesNames()
                    .contains(i) && givenBranchCommit.getFilesNames().contains(i)
                    && currBranchCommit.getMapFromNameToHash()
                    .get(i).equals(splitPoint.getMapFromNameToHash().get(i))
                    && !givenBranchCommit.getMapFromNameToHash()
                    .get(i).equals(splitPoint.getMapFromNameToHash().get(i))) {
                filesShouldOverDirect.add(i);
            }
        }

        for (String i : splitPoint.getFilesNames()) {
            if (currBranchCommit.getFilesNames().contains(i)
                    && currBranchCommit.getMapFromNameToHash()
                    .get(i).equals(splitPoint.getMapFromNameToHash().get(i))
                    && !givenBranchCommit.getFilesNames().contains(i)) {
                filesToRemove.add(i);
            }
        }

        for (String i : givenBranchCommit.getFilesNames()) {
            if (!splitPoint.getFilesNames()
                    .contains(i) && !currBranchCommit
                    .getFilesNames().contains(i)) {
                filesOnlyInGiven.add(i);
            }
        }

    }
    public boolean flagGet(Commit givenBranchCommit, Commit splitPoint,
                           Commit currBranchCommit, ArrayList<String> conflicts1,
                           ArrayList<String> conflicts2) {
        boolean flag1 = true;
        for (String i : givenBranchCommit.getFilesNames()) {
            if (currBranchCommit.getFilesNames().contains(i)
                    && splitPoint.getFilesNames().contains(i)
                    && !currBranchCommit.getMapFromNameToHash()
                    .get(i).equals(splitPoint.getMapFromNameToHash().get(i))
                    && !givenBranchCommit.getMapFromNameToHash()
                    .get(i).equals(splitPoint.getMapFromNameToHash().get(i))
                    && !currBranchCommit.getMapFromNameToHash()
                    .get(i).equals(givenBranchCommit.getMapFromNameToHash().get(i))) {
                conflicts1.add(i);
                flag1 = false;
            }
        }
        for (String i : splitPoint.getFilesNames()) {
            if ((currBranchCommit.getFilesNames().contains(i)
                    && !currBranchCommit.getMapFromNameToHash()
                    .get(i).equals(splitPoint.getMapFromNameToHash().get(i))
                    && !givenBranchCommit.getFilesNames().contains(i))
                    || (givenBranchCommit.getFilesNames().contains(i) && !givenBranchCommit
                    .getMapFromNameToHash().get(i).equals(splitPoint
                            .getMapFromNameToHash().get(i))
                    && !currBranchCommit.getFilesNames().contains(i))) {
                conflicts2.add(i);
                flag1 = false;
            }
        }
        for (String i : givenBranchCommit.getFilesNames()) {
            if (currBranchCommit.getFilesNames().contains(i)
                    && !splitPoint.getFilesNames().contains(i)
                    && !currBranchCommit.getMapFromNameToHash()
                    .get(i).equals(givenBranchCommit.getMapFromNameToHash().get(i))) {
                conflicts1.add(i);
                flag1 = false;
            }
        }
        return flag1;
    }

    public void addRemove(ArrayList<String> filesShouldOverDirect,
                          Commit givenBranchCommit, boolean flag1,
                          ArrayList<String> filesOnlyInGiven, ArrayList<String> filesToRemove) {
        for (String i : filesShouldOverDirect) {
            File changedFile = new File(i);
            File originFile = new File(givenBranchCommit.getCommitParh()
                    + "/" + givenBranchCommit
                    .getMapFromNameToHash().get(i));
            Utils.writeContents(changedFile, Utils.readContents(originFile));
            if (flag1) {
                this.add(i);
            }
        }
        for (String i : filesOnlyInGiven) {
            File changedFile = new File(i);
            File originFile = new File(givenBranchCommit.getCommitParh()
                    + "/" + givenBranchCommit
                    .getMapFromNameToHash().get(i));
            Utils.writeContents(changedFile, Utils.readContents(originFile));
            this.add(i);
        }
        for (String i : filesToRemove) {
            File delFile = new File(i);
            delFile.delete();
            currBranch.remove(i);
            Main.saveCommitTree();
        }
    }

    public void iniCheck(Commit givenBranchCommit, ArrayList<String> toTest,
                         ArrayList<String> toTest1, ArrayList<String> toTest2) {
        for (String i : givenBranchCommit.getFilesNames()) {
            toTest.add(i);
            toTest1.add(i);
        }
        for (String i : toTest) {
            if (givenBranchCommit.getUnFile().contains(i)) {
                toTest1.remove(i);
            }
        }
        ArrayList<String> folderNames = new ArrayList<>();
        for (String i:Utils.plainFilenamesIn(System.getProperty("user.dir"))) {
            folderNames.add(i);
        }
        for (String i : toTest1) {
            if (folderNames.contains(i)) {
                toTest2.add(i);
                if (Utils.sha1(Utils.readContents(new File(i)))
                        .equals(givenBranchCommit.getMapFromNameToHash().get(i))) {
                    toTest2.remove(i);
                }
            }
        }
    }

    public Commit mergeHelp(Commit givenBranch1, Commit currBranch1) {
        Commit givenBranch = givenBranch1;
        Commit currBranch2 = currBranch1;
        Commit temp = currBranch2;
        while (givenBranch != null) {
            currBranch2 = temp;
            while (currBranch2 != null) {
                if (givenBranch.getHashId().equals(currBranch2.getHashId())) {
                    return currBranch2;
                }
                currBranch2 = currBranch2.getParent();
            }
            givenBranch = givenBranch.getParent();
        }
        return null;
    }


    public static byte[] unitByteArray(byte[] byte1, byte[] byte2) {
        byte[] unitByte = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, unitByte, 0, byte1.length);
        System.arraycopy(byte2, 0, unitByte, byte1.length, byte2.length);
        return unitByte;
    }

}

