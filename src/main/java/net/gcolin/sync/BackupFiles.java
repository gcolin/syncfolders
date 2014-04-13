package net.gcolin.sync;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gcolin.sync.model.BackupModel;
import net.gcolin.sync.model.BackupState;
import javafx.beans.binding.ObjectBinding;

public class BackupFiles extends ObjectBinding<BackupState> {
    
    private final static boolean test = false;
    
    private static class BackupFilesVisitor extends SimpleFileVisitor<Path>{
        private Map<String, File> currents = new HashMap<String, File>();
        private Map<String, File> currentsFolders = new HashMap<String, File>();
        private String prefix;
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            File f = file.toFile();
            currents.put(f.getPath().substring(prefix.length() + 1), f);

            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
            File f = dir.toFile();
            if (f.getPath().length() > prefix.length() + 1) {
                currentsFolders.put(f.getPath().substring(prefix.length() + 1), f);
            }
            return FileVisitResult.CONTINUE;
        }
    }
    
    public static Logger log = Logger.getLogger(BackupFiles.class.getName());

    private BackupState state = new BackupState();
    private long copied = 0;
    private long total = 0;
    
    public void backup(BackupModel model) {
        try {
            state.setState(BackupState.STATE.SYNC);
            invalidate();
            log.info("prepare backup");

            File dir1 = new File(model.getDir1());
            File dir2 = new File(model.getDir2());
            BackupFilesVisitor bf1 = new BackupFilesVisitor();
            bf1.prefix = dir1.getPath();
            Path path = Paths.get(dir1.getPath());
            Files.walkFileTree(path, bf1);
            log.info("read files from source : " + bf1.currents.size());
            BackupFilesVisitor bf2 = new BackupFilesVisitor();
            bf2.prefix = dir2.getPath();
            path = Paths.get(dir2.getPath());
            Files.walkFileTree(path, bf2);
            log.info("read files from destination : " + bf2.currents.size());

            Set<String> examinated = new HashSet<String>();
            List<Copy> tasks = new ArrayList<>();
            total = 0;
            long percent = 0;
            int i = 0;
            int l = bf1.currents.size();
            state.setTimeInitial(System.currentTimeMillis());

            log.info("compare files");
            for (Entry<String, File> entry : bf1.currents.entrySet()) {
                examinated.add(entry.getKey());
                File f2 = bf2.currents.get(entry.getKey());
                if (f2 == null) {
                    f2 = new File(dir2, entry.getKey());
                    total += entry.getValue().length();
                    tasks.add(new Copy(entry.getValue(), f2, false));
                } else if (f2.lastModified() < entry.getValue().lastModified()
                        || f2.length() < entry.getValue().length()) {
                    total += entry.getValue().length();
                    tasks.add(new Copy(entry.getValue(), f2, true));
                }
                if (i * 100 / l != percent) {
                    percent = i * 100 / l;
                    state.setPercent((int) percent);
                    invalidate();
                }
                i++;
            }
            if (total == 0) {
                total = 1;
            }

            log.info("number of files to copy : " + tasks.size());

            int nbToDelete = 0;
            for (Entry<String, File> entry : bf2.currents.entrySet()) {
                if (examinated.contains(entry.getKey())) {
                    continue;
                }
                nbToDelete++;
            }

            log.info("number of files to delete : " + nbToDelete);

            copied = 0;
            percent = 0;
            state.setTimeInitial(System.currentTimeMillis());

            state.setState(BackupState.STATE.COPY);
            log.info("copy files");
            for (Copy cp : tasks) {
                cp.run();
                copied += cp.src.length();
                if (copied * 100 / total != percent) {
                    state.setPercent((int) (copied * 100 / total));
                    invalidate();
                }
            }

            state.setState(BackupState.STATE.DELETE);
            invalidate();
            log.info("finishing backup");

            for (Entry<String, File> entry : bf2.currents.entrySet()) {
                if (examinated.contains(entry.getKey())) {
                    continue;
                }
                log.info("delete " + entry.getKey());
                if(!test)
                {
                    entry.getValue().delete();
                }
            }

            for (Entry<String, File> entry : bf2.currentsFolders.entrySet()) {
                File f1 = bf1.currentsFolders.get(entry.getKey());
                if (f1 == null) {
                    log.info("delete " + entry.getValue());
                    if(!test)
                    {
                        Files.delete(entry.getValue().toPath());
                    }
                }
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        state.setState(BackupState.STATE.END);
        invalidate();
    }

    class Copy implements Runnable {

        File src;
        File dest;
        boolean update;
        static final long MIN_SIZE_TO_OBSERVE = 1024*1024*50;

        public Copy(File src, File dest, boolean update) {
            this.src = src;
            this.dest = dest;
            this.update = update;
        }

        @Override
        public void run() {
            try {
                if (update) {
                    log.info("update " + src.getPath());
                    dest.delete();
                } else {
                    dest.getParentFile().mkdirs();
                    log.info("copy " + src.getPath());
                }
                if(!test)
                {
                    long l = src.length();
                    state.setPercentCurrentFile(0);
                    if(l<MIN_SIZE_TO_OBSERVE){
                        Files.copy(src.toPath(), dest.toPath());
                    }else
                    {
                        try(InputStream bin = new ProgessInputStream(1024*1024,new BufferedInputStream(new FileInputStream(src)),
                                (Long c)->{
                                    int p = (int)(c*100l/l);
                                    if(p!=state.getPercentCurrentFile())
                                    {
                                        state.setPercentCurrentFile(p);
                                        state.setPercent((int)((copied+c) * 100l / total));
                                        invalidate();
                                    }
                                })){
                            Files.copy(bin, dest.toPath());
                        }
                    }
                    
                }
                dest.setLastModified(src.lastModified());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    protected BackupState computeValue() {
        return (BackupState) state.clone();
    }

    
}
