package application;

import java.io.IOException;

import application.io.Duplicate;
import application.io.Sync;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;

public class Controller {

	private Stage primaryStage;
	private Sync sync = new Sync();
	private Duplicate dup = new Duplicate();
	private Duplicate dupRemoveSameFolder = new Duplicate();
	private Sync syncSim = new Sync() {
		
		protected void send(application.io.Item next, application.io.Connection csrc, application.io.Connection cdest)
				throws IOException {
			String x = "add " + cdest.path() + "/" + next.getName();
			Platform.runLater(
					() -> webEngine.executeScript("window.bus.list(\"" + x.replaceAll("\"", "\\\"") + "\");"));
		}

		protected void remove(application.io.Item next, application.io.Connection cdest) throws IOException {
			String x = "del " + cdest.path() + "/" + next.getName();
			Platform.runLater(
					() -> webEngine.executeScript("window.bus.list(\"" + x.replaceAll("\"", "\\\"") + "\");"));
		}
	};
	private WebEngine webEngine;

	public Controller(Stage primaryStage, WebEngine webEngine) {
		this.primaryStage = primaryStage;
		this.webEngine = webEngine;
		sync.setMessageConsumer(x -> {
			Platform.runLater(
					() -> webEngine.executeScript("window.bus.message(\"" + x.replaceAll("\"", "\\\"") + "\");"));
		});
		syncSim.setMessageConsumer(sync.getMessageConsumer());
		dup.setDuplicateConsumer(x -> {
			StringBuilder str = new StringBuilder("window.bus.dup([");
			for (int i = 0; i < x.length; i++) {
				if (i > 0) {
					str.append(',');
				}
				str.append('"');
				str.append(x[i].getPath().replaceAll("\"", "\\\""));
				str.append('"');
			}
			str.append("]);");
			Platform.runLater(() -> webEngine.executeScript(str.toString()));
		});
		dup.setPercentConsumer(x -> {
			Platform.runLater(() -> webEngine.executeScript("window.bus.percent(" + x + ")"));
		});
		dupRemoveSameFolder.setPercentConsumer(dup.getPercentConsumer());
		dupRemoveSameFolder.setDuplicateConsumer(dup.getDuplicateConsumer());
		dupRemoveSameFolder.enableRemove();
	}

	public void duplicate(String src, boolean checkSize, boolean checkContent) {
		duplicate0(dup, src, checkSize, checkContent);
	}

	public void removeSameFolder(String src, boolean checkSize, boolean checkContent) {
		duplicate0(dupRemoveSameFolder, src, checkSize, checkContent);
	}

	public void duplicate0(Duplicate d, String src, boolean checkSize, boolean checkContent) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					d.duplicate(sync, src, checkSize, checkContent);
				} catch (IOException e) {
					error(e);
				} finally {
					Platform.runLater(() -> webEngine.executeScript("window.bus.end();"));
				}

			}
		}).start();
	}

	public void synchro0(Sync sync, String src, String dest, boolean checkSize, boolean checkDate,
			boolean checkContent) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					sync.sync(src, dest, checkSize, checkDate, checkContent);
				} catch (IOException e) {
					error(e);
				} finally {
					Platform.runLater(() -> webEngine.executeScript("window.bus.end();"));
				}

			}
		}).start();
	}

	public void error(IOException e) {
		Platform.runLater(() -> webEngine.executeScript(
				"window.bus.error(\"" + e.toString().replaceAll("\"", "\\\"").replaceAll("\n", "\\n") + "\");"));
	}

	public void synchro(String src, String dest, boolean checkSize, boolean checkDate, boolean checkContent) {
		synchro0(sync, src, dest, checkSize, checkDate, checkContent);
	}

	public void synchroSim(String src, String dest, boolean checkSize, boolean checkDate, boolean checkContent) {
		synchro0(syncSim, src, dest, checkSize, checkDate, checkContent);
	}

	public void setTitle(String t) {
		primaryStage.setTitle(t);
	}
}
