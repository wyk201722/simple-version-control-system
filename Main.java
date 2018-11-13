package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author
 */
public class Main {
    static CommitTree commitTree = new CommitTree();
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String opea1 = "";
        if (args.length > 1) {
            opea1 = args[1];
        }
        String opea2 = "";
        if (args.length > 2) {
            opea2 = args[2];
        }
        String operator = args[0];
        commitTree = loadCommitTree();
        /**  decide what kind of operation should be done.*/
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            switch (operator) {
                case "init":
                    if (!isAlreadyIni()) {
                        commitTree = initilize();
                        saveCommitTree();
                    } else {
                        System.out.println("A gitlet version-control system already "
                                + "exists in the current directory.");
                    }
                    break;
                case "add":
                    commitTree.add(opea1);
                    saveCommitTree();
                    break;
                case "commit":
                    if (args.length == 2) {
                        if (args[1].equals("")) {
                            System.out.println("Please enter a commit message.");
                        } else {
                            commitTree.commit(args[1]);
                            saveCommitTree();
                        }
                    } else if (args.length == 1) {
                        System.out.println("Please enter a commit message.");
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "rm":
                    commitTree.remove(opea1);
                    saveCommitTree();
                    break;
                case "log":
                    commitTree.logPrint();
                    break;
                case "global-log":
                    commitTree.globalPrint();
                    break;
                case "find":
                    commitTree.printFind(args[1]);
                    break;
                case "status":
                    commitTree.printStatus();
                    break;
                case "checkout":
                    if ((args.length == 3) && (args[1].equals("--"))) {
                        commitTree.checkOutFromCommit(args[2]);
                    } else if ((args.length == 4) && (args[2].equals("--"))) {
                        if (args[1].length() > 8) {
                            commitTree.checkOutFromFolder(args[1], args[3]);
                        } else {
                            commitTree.checkOutShort(args[1], args[3]);
                        }
                    } else if (args.length == 2) {
                        commitTree.checkOutBranch(args[1]);
                        saveCommitTree();
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    break;
                case "branch":
                    commitTree.branch(opea1);
                    saveCommitTree();
                    break;
                case "rm-branch":
                    commitTree.rmBranch(opea1);
                    saveCommitTree();
                    break;
                case "reset":
                    commitTree.reset(opea1);
                    saveCommitTree();
                    break;
                case "merge":
                    commitTree.merge(opea1);
                    saveCommitTree();
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }
    }
    private static CommitTree initilize() {
        File f = new File(".gitlet");
        f.mkdirs();
        return CommitTree.commitTreeInit();
    }
    public static boolean isAlreadyIni() {
        File f = new File(".gitlet");
        return f.exists();
    }
    public static void saveCommitTree() {
        Object obj = commitTree;
        File outFile = new File(commitTree.getTreeRepo());
        try {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(obj);
            out.close();
        } catch (IOException excp) {
            System.out.println("Ives is a idiot");
        }
    }
    private static CommitTree loadCommitTree() {

        CommitTree obj;
        File inFile = new File(commitTree.getTreeRepo());
        if (inFile.exists()) {
            try {

                ObjectInputStream inp =
                        new ObjectInputStream(new FileInputStream(inFile));
                obj = (CommitTree) inp.readObject();
                inp.close();
            } catch (IOException | ClassNotFoundException excp) {
                obj = null;
            }
            return (CommitTree) obj;
        } else {
            return null;
        }
    }
}


