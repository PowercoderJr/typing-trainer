package typingtrainer.Game;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import typingtrainer.GameScene.GameSceneController;

/**
 * Created by Meow on 12.05.2017.
 */
public class WoodenSplinter extends PvpObject
{
	public static final double MAX_ROTATION_SPEED = 360.0;
	public static final double MIN_FLY_SPEED = Cannonball.BASE_SPEED / 3;
	public static final double MAX_FLY_SPEED = Cannonball.BASE_SPEED / 2;
	public static final double MIN_FLY_TIME = 500;
	public static final double MAX_FLY_TIME = 2000;
	public static final double FINAL_SCALE = 0.6;

	private double rotationSpeed;
	private double flySpeed;
	private double flyTime;
	private Point2D flyDir;
	private boolean isFlightEnded;

	private double scalingSpeed;

	public WoodenSplinter(Belonging belonging, Point2D position, Image image)
	{
		super(belonging, position);
		this.image = image;
		pivot = new Point2D(image.getWidth() / 2, image.getHeight() / 2);
		rotationSpeed = -MAX_ROTATION_SPEED + Math.random() * 2 * MAX_ROTATION_SPEED;
		flySpeed = MIN_FLY_SPEED + Math.random() * (MAX_FLY_SPEED - MIN_FLY_SPEED);
		flyTime = MIN_FLY_TIME + Math.random() * (MAX_FLY_TIME - MIN_FLY_TIME);
		flyDir = new Point2D(0.2 + Math.random(), -0.5 + Math.random()).normalize();
		scalingSpeed = (FINAL_SCALE - scale) / flyTime * 1000;
		isFlightEnded = false;
	}

	public void flyingTick(int dt)
	{
		if (flyTime > 0)
		{
			rotationAngle += rotationSpeed / 1000 * dt;
			setScale(scale + scalingSpeed / 1000 * dt);
		}
		else if (flyTime > -1000)
		{
			flyDir = flyDir.add(0, 0.1).normalize();
			flySpeed = GameSceneController.BACKGROUND_SPEED + (flySpeed - GameSceneController.BACKGROUND_SPEED) / 2;
			rotationSpeed /= 2;
		}
		else if (!isFlightEnded)
		{
			flyDir = new Point2D(0, 1);
			flySpeed = GameSceneController.BACKGROUND_SPEED;
			rotationSpeed = 0;
			isFlightEnded = true;
		}
		flyTime -= dt;
		position = position.add(flyDir.getX() * flySpeed / 1000 * dt, flyDir.getY() * flySpeed / 1000 * dt);
	}
}