package application;

import java.io.IOException;

import application.io.Duplicate;
import application.io.Sync;
import javafx.application.Application;

public class Main {
	
	private static int nb = 0;
	
	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			Application.launch(Ui.class,args);
		} else {
			String command = args[0];
			switch(command) {
			case "help":
			case "?":
				printHelp();
				break;
			case "sync" : 
				sync(args);
				break;
			case "dup" : 
				dup(args);
				break;
			}
		}
	}

	private static void dup(String[] args) throws IOException {
		if(args.length<2) {
			printHelp();
		}else {
			String options = args[1];
			String src = null;
			boolean checkSize = false;
			boolean checkContent = false;
			boolean remove = false;
			
			if(options.startsWith("-")) {
				if(args.length<3) {
					printHelp();
					return;
				}
				checkSize = options.contains("l");
				checkContent = options.contains("c");
				remove = options.contains("r");
				src = args[2];
			}else {
				src = args[1];
			}
			
			Duplicate dup = new Duplicate();
			dup.setPercentConsumer(x->{});
			dup.setDuplicateConsumer(x -> {
				nb++;
				for(int i=0;i<x.length;i++) {
					System.out.println(x[i].getPath());
				}
				System.out.println();
			});
			
			if(remove) {
				dup.enableRemove();
			}
			
			dup.duplicate(new Sync(), src, checkSize, checkContent);
			
			if(nb == 0){
				System.out.println("No Duplicate file");
			}else if(nb == 1){
				System.out.println("1 Duplicate file");
			} else {
				System.out.println(nb+" Duplicate files");
			}
		}
	}

	private static void sync(String[] args) throws IOException {
		if(args.length<3) {
			printHelp();
		}else {
			String options = args[1];
			String dest = null;
			String src = null;
			boolean checkSize = false;
			boolean checkDate = false;
			boolean checkContent = false;
			boolean simulate = false;
			
			if(options.startsWith("-")) {
				if(args.length<4) {
					printHelp();
					return;
				}
				checkSize = options.contains("l");
				checkDate = options.contains("t");
				checkContent = options.contains("c");
				simulate = options.contains("s");
				dest = args[3];
				src = args[2];
			}else {
				dest = args[2];
				src = args[1];
			}
			
			Sync sync = null;
			
			if(simulate) {
				sync = new Sync() {
					protected void send(application.io.Item next, application.io.Connection csrc, application.io.Connection cdest)
							throws IOException {
						System.out.println("add " + cdest.path() + "/" + next.getName());
					};

					protected void remove(application.io.Item next, application.io.Connection cdest) throws IOException {
						System.out.println("del " + cdest.path() + "/" + next.getName());
					};
				};
			} else {
				sync = new Sync();
			}
			sync.sync(src, dest, checkSize, checkDate, checkContent);
		}		
	}

	private static void printHelp() {
		System.out.println("Usage : ");
		System.out.println();
		System.out.println("  - For user interface   : java -jar sync.jar");
		System.out.println();
		System.out.println("  - For synchronization  : java -jar sync.jar sync [OPTIONS] [SRC] [DEST]");
		System.out.println("    Synchronization copy files from SRC folder to DEST folder in an intelligent way");
		System.out.println("     OPTIONS :");
		System.out.println("       s : simulate (display actions that can be performed)");
		System.out.println("       l : check file size");
		System.out.println("       t : check file timestamp");
		System.out.println("       c : check file content");
		System.out.println("     SRC : source folder or ftp url");
		System.out.println("     DEST : destination folder or ftp url");
		System.out.println("     Example : java -jar sync.jar sync -ltc /home/user/Documents ftp://username:password@server.com/backupDocument");
		System.out.println();
		System.out.println("  - For duplicate finding  : java -jar sync.jar dup [OPTIONS] [SRC]");
		System.out.println("    Duplicate finding look up similar files in SRC folder and print them");
		System.out.println("     OPTIONS :");
		System.out.println("       r : remove duplicate file if they are in the same folder (only keep the file with the smallest name)");
		System.out.println("       l : check file size");
		System.out.println("       c : check file content");
		System.out.println("     SRC : source folder or ftp url");
		System.out.println("     Example : java -jar sync.jar dup -rtc /home/user/Documents");
	}
}
