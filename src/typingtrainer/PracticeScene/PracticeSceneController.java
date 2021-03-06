package typingtrainer.PracticeScene;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import typingtrainer.Main;
import typingtrainer.MainScene.MainSceneController;
import typingtrainer.ManagedScene;
import typingtrainer.PracticeWatcher;
import typingtrainer.Word;
import typingtrainer.CongScene.CongSceneController;

import java.awt.im.InputContext;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Meow on 25.02.2017.
 */
public class PracticeSceneController
{
	@FXML
	public GridPane pane;
	@FXML
	public Label mainMenuLabel;
	@FXML
	public Label displayableStringLabel;
	@FXML
	public Label timerLabel;
	@FXML
	public Rectangle highlightRct;
	@FXML
	public Rectangle highlightLShiftRct;
	@FXML
	public Rectangle highlightRShiftRct;
	@FXML
	public Rectangle highlightSpaceRct;
	@FXML
	public ImageView musicImg;
	@FXML
	public ImageView soundImg;

	private static final int VOLUME_REDUCING_DELAY = 2000;
	private static final int VOLUME_REDUCING_STEP = 50;
	private static final int SECONDS_MINUTE_CONTAIN = 60;
	private static final double SECONDS_NANOSECOND_CONTAIN = 1e-9;
	private static final Rectangle2D MUSIC_ON_RECT = new Rectangle2D(0, 0, 100, 100);
	private static final Rectangle2D MUSIC_OFF_RECT = new Rectangle2D(100, 0, 100, 100);
	private static final Rectangle2D SOUND_ON_RECT = new Rectangle2D(200, 0, 100, 100);
	private static final Rectangle2D SOUND_OFF_RECT = new Rectangle2D(300, 0, 100, 100);

	private PracticeWatcher watcher;
	private volatile MediaPlayer music;
	private volatile MediaPlayer falseNote;
	private volatile int msToReducing;
	private volatile boolean isTimerRunning;

	private static Word.Languages paramLang;
	private static int difficultyParam;
	private static boolean isRegisterParam;
	private static boolean isMusicParam;
	private static boolean isSoundParam;
	private static int[][] keyCoordinates = {
			{396, 180},        //а
			{649, 180},        //о
			{311, 180},        //в
			{733, 180},        //л
			{227, 180},        //ы
			{817, 180},        //д
			{143, 180},        //ф
			{901, 180},        //ж
			{480, 180},        //п
			{564, 180},        //р
			{370, 93},        //к
			{623, 93},        //г
			{455, 93},        //е
			{539, 93},        //н
			{426, 268},        //м
			{682, 268},        //ь
			{514, 268},        //и
			{598, 268},        //т
			{286, 93},        //у
			{708, 93},        //ш
			{345, 268},        //с
			{767, 268},        //б
			{202, 93},        //ц
			{792, 93},        //щ
			{261, 268},        //ч
			{851, 268},        //ю
			{117, 93},        //й
			{876, 93},        //з
			{176, 268},        //я
			{936, 268},        //.
			{961, 93},        //х
			{1045, 93},        //ъ
			{986, 180},        //э
	};

	public void initialize()
	{

		System.out.println("Сцена практики готова!");
		watcher = new PracticeWatcher(paramLang, difficultyParam, isRegisterParam);
		restart();
		InputContext InCon = java.awt.im.InputContext.getInstance();
		InCon.selectInputMethod(new Locale("en", "US"));
		musicImg.setImage(MainSceneController.BUTTONS_SPRITESHEET);
		musicImg.setViewport(isMusicParam ? MUSIC_ON_RECT : MUSIC_OFF_RECT);
		soundImg.setImage(MainSceneController.BUTTONS_SPRITESHEET);
		soundImg.setViewport(isSoundParam ? SOUND_ON_RECT : SOUND_OFF_RECT);
	}

	private void restart()
	{
		disposeSounds();
		watcher.restart();
		updHighlights();
		displayableStringLabel.setText(watcher.getDisplayableString());

		music = null;
		File musicFolder = new File("music");
		if (musicFolder.exists())
		{
			File musicFiles[] = musicFolder.listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return name.toLowerCase().endsWith(".mp3");
				}
			});
			if (musicFiles.length > 0)
				music = new MediaPlayer(new Media(musicFiles[(int) (Math.random() * musicFiles.length)].toURI().toString()));
		}
		if (music == null)
		{
			isMusicParam = false;
			musicImg.setVisible(false);
		}
		msToReducing = 0;
		isTimerRunning = false;
	}

	public void onMainMenuLabelClicked(MouseEvent mouseEvent)
	{
		disposeSounds();
		isTimerRunning = false;
		try
		{
			Main.sceneManager.popAllExceptFirst();
		}
		catch (InvocationTargetException e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void onKeyPressed(KeyEvent keyEvent) throws IOException
	{
		if (!keyEvent.getCode().toString().equals("CONTROL") &&
				!keyEvent.getCode().toString().equals("SHIFT") &&
				!keyEvent.getCode().toString().equals("ALT") &&
				!keyEvent.getCode().toString().equals("ALT_GRAPH"))
		{
			if (!isTimerRunning)
			{
				isTimerRunning = true;
				watcher.rememberTimeStart();
				new Thread(() ->
				{
					//BEGIN
					int min = 0, sec = 0;
					while (isTimerRunning)
					{
						for (int i = 0; isTimerRunning && i < 20; ++i)
						{
							try
							{
								Thread.sleep(50);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
						if (++sec == 60)
						{
							sec = 0;
							++min;
						}
						final String newTime = (min > 9 ? "" : "0") + min + ":" + (sec > 9 ? "" : "0") + sec;
						Platform.runLater(() -> timerLabel.setText(newTime));
					}
					Platform.runLater(() -> timerLabel.setText("00:00"));
					//END
				}).start();
			}

			boolean isSymbolCorrect;
			if (!keyEvent.getText().isEmpty())
			{
				if (watcher.getCurrentChar() == ' ')
				{
					isSymbolCorrect = keyEvent.getText().charAt(0) == ' ';
				}
				else
				{
					if (keyEvent.isShiftDown())
					{
						char symbolWithShift;
						String[] alphabet;
						switch (watcher.getLang())
						{
							case RU:
							default:
								alphabet = Word.ALPH_RU;
								break;
							case EN:
								alphabet = Word.ALPH_EN;
								break;
						}
						int symbolIndex = alphabet[0].indexOf(keyEvent.getText().charAt(0));
						if (symbolIndex >= 0 && symbolIndex < Word.MAX_LEVEL)
						{
							symbolWithShift = alphabet[1].charAt(symbolIndex);
							isSymbolCorrect = symbolWithShift == watcher.getCurrentChar();
						}
						else
						{
							isSymbolCorrect = false;
						}
					}
					else
					{
						isSymbolCorrect = keyEvent.getText().charAt(0) == watcher.getCurrentChar();
					}
				}
			}
			else
				isSymbolCorrect = false;

			if (isSymbolCorrect)
			{
				if (isMusicParam)
					playGoodMusic();
				watcher.passCurrentChar();
				if (watcher.getDisplayableString().length() != 0)
				{
					displayableStringLabel.setText(watcher.getDisplayableString());
					updHighlights();
				}
				else
				{
					disposeSounds();
					isTimerRunning = false;
					displayableStringLabel.setText("");

					int mistakes = watcher.getMistakeCount();
					double time = watcher.getFinalTime() * PracticeSceneController.SECONDS_NANOSECOND_CONTAIN;
					int speed = (int) (watcher.getInitStringLength() * PracticeSceneController.SECONDS_MINUTE_CONTAIN / time);
					boolean isStatisticsSaved = true;
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
					String date = dateFormat.format(new Date());

					try (FileWriter st_write = new FileWriter("statistics.txt", true))
					{
						st_write.write((mistakes + "\r\n" + time + "\r\n" + speed + "\r\n" + date + "\r\n"));
						st_write.flush();
						st_write.close();
					}
					catch (IOException e)
					{
						isStatisticsSaved = false;
					}

					CongSceneController.SetResults(speed, time, mistakes, isStatisticsSaved);
					Parent congSceneFXML = FXMLLoader.load(Main.class.getResource("CongScene/congScene.fxml"));
					ManagedScene congScene = new ManagedScene(congSceneFXML, Main.DEFAULT_SCREEN_WIDTH, Main.DEFAULT_SCREEN_HEIGHT, Main.sceneManager);
					congScene.getStylesheets().add("typingtrainer/CongScene/style.css");
					Main.sceneManager.pushScene(congScene);
				}
			}
			else
			{
				if (isSoundParam)
					playBadMusic();
				if (music != null)
					music.pause();
				watcher.addMistake();
			}
		}
	}

	private void playGoodMusic()
	{
		if (music != null)
			music.play();
		if (msToReducing > 0)
			msToReducing = VOLUME_REDUCING_DELAY;
		else
		{
			new Thread(() ->
			{
				msToReducing = VOLUME_REDUCING_DELAY;
				//Задержка до начала снижения громкости
				while (msToReducing > 0)
				{
					msToReducing -= VOLUME_REDUCING_STEP;
					try
					{
						Thread.sleep(VOLUME_REDUCING_STEP);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				//Постепенное снижение громкости
				while (msToReducing == 0 && music != null && music.getVolume() > 0)
				{
					music.setVolume(music.getVolume() - 0.1);
					try
					{
						Thread.sleep(VOLUME_REDUCING_STEP);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

				if (music != null)
				{
					if (msToReducing == 0)
						music.pause();
					music.setVolume(1.0);
				}
			}).start();
		}
	}

	private void playBadMusic()
	{
		if (falseNote != null)
		{
			MediaPlayer buf = falseNote;
			new Thread(() ->
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				buf.dispose();
			}).start();
		}
		try
		{
			falseNote = new MediaPlayer(new Media(Main.class.getResource("PracticeScene/sounds/false_note_" + (int) (1 + Math.random() * 5) + ".mp3").toURI().toString()));
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
		falseNote.play();
	}

	public static void setOptions(Word.Languages lang, int difficulty, boolean isRegister, boolean isMusic, boolean isSound)
	{
		paramLang = lang;
		difficultyParam = difficulty;
		isRegisterParam = isRegister;
		isMusicParam = isMusic;
		isSoundParam = isSound;
	}

	private void disposeSounds()
	{
		msToReducing = 0;
		if (music != null)
		{
			music.stop();
			music.dispose();
			music = null;
		}
		if (falseNote != null)
		{
			falseNote.stop();
			falseNote.dispose();
			falseNote = null;
		}
	}

	private void updHighlights()
	{
		char currChar = watcher.getCurrentChar();
		boolean isShift;
		if (currChar == ' ')
		{
			highlightRct.setVisible(false);
			highlightLShiftRct.setVisible(false);
			highlightRShiftRct.setVisible(false);
			highlightSpaceRct.setVisible(true);
		}
		else
		{
			String[] alphabet;
			switch (watcher.getLang())
			{
				case RU:
				default:
					alphabet = Word.ALPH_RU;
					break;
				case EN:
					alphabet = Word.ALPH_EN;
			}
			int i;
			if (alphabet[0].indexOf(currChar) != -1)
			{
				i = alphabet[0].indexOf(currChar);
				isShift = false;
			}
			else
			{
				i = alphabet[1].indexOf(currChar);
				isShift = true;
			}

			highlightRct.setVisible(true);
			highlightRct.setLayoutX(keyCoordinates[i][0]);
			highlightRct.setLayoutY(keyCoordinates[i][1]);
			highlightLShiftRct.setVisible(isShift);
			highlightRShiftRct.setVisible(isShift);
			highlightSpaceRct.setVisible(false);
		}
	}

	public void onRestartLabelClicked(MouseEvent mouseEvent)
	{
		restart();
	}

	public void onMusicImgClicked(MouseEvent mouseEvent)
	{
		isMusicParam = !isMusicParam;
		musicImg.setViewport(isMusicParam ? MUSIC_ON_RECT : MUSIC_OFF_RECT);
		if (music != null)
			music.pause();
	}

	public void onSoundImgClicked(MouseEvent mouseEvent)
	{
		isSoundParam = !isSoundParam;
		soundImg.setViewport(isSoundParam ? SOUND_ON_RECT : SOUND_OFF_RECT);
	}
}