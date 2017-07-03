package TankGame18;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.util.*;

class BoomEffect
{
	/*
	 * Pixel Position
	 */
	protected Coordinate position;
	protected boolean isLive;
	protected int life;
	protected Vector<Image> effects = new Vector<Image>();
	protected Coordinate effectSize;
	public BoomEffect(Coordinate position)
	{
		this.position=position;
		isLive=true;
	}
	
	public Image showCurrentEffect()
	{
		return null;
	}
	
	public Coordinate getPosition()
	{
		return position;
	}
	
	public Coordinate getEffectSize()
	{
		return effectSize;
	}
	
}

class HardWallHitting extends BoomEffect
{

	public HardWallHitting(Coordinate position){
		super(position);
		life = 5;
		effectSize=new Coordinate(10,10);
		Image im_1=Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/hithardwallstar.png"));
		Image im_2=Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/hithardwallstar.png"));
		effects.add(im_1);
		effects.add(im_2);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Image showCurrentEffect()
	{
		life --;
		if(life>2)
		{
			return effects.get(0);
		}
		else
		{
			if(life==0)
			{
				this.isLive=false;
			}
			return effects.get(1);
		}
	}
}

class TankExplosion extends BoomEffect
{
	public TankExplosion(Coordinate position){
		super(position);
		life=12;
		effectSize=new Coordinate(30,30);
		// TODO Auto-generated constructor stub
		for(int i=1;i<7;i++)
		{
			String name = "/bomb_"+Integer.toString(i)+".png";
			Image im_1=
					Toolkit.getDefaultToolkit().getImage
					 (Panel.class.getResource(name));
			effects.add(im_1);
		}
	}
	
	@Override
	public Image showCurrentEffect()
	{
	life--;
	if(life>9)
	{
		return effects.get(0);
	}else if(life >7)
	{
		return effects.get(1);
	}else if(life >5)
	{
		return effects.get(2);
	}else if(life>3)
	{
		return effects.get(3);
	}else if(life>1)
	{
		return effects.get(4);
	}
	else
	{
		this.isLive=false;
		return effects.get(5);
	}
	}
}

class Coordinate
{
	int x;
	int y;
	public Coordinate(int x, int y)
	{
		this.x=x;
		this.y=y;
	}
	public Coordinate()
	{
		
	}
}

class Command
{
	boolean fireEnemy;
	int direction;
	
	public Command(boolean fire, int direct)
	{
		this.fireEnemy=fire;
		this.direction=direct;
	}
	
	public Command()
	{
		
	}
}

/*
 * the Position of a bullet is calculated in original pixels.
 */

class Bullet implements Runnable
{
	private int x;
	private int y;
	private int direction;
	private boolean isLive;
	private char type;
	static int SPEED = 10;
	
	public char getType()
	{
		return type;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public boolean isLive() {
		return isLive;
	}

	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}
	
	public Bullet(int x, int y, int direction, char type)
	{
		this.x=x;
		this.y=y;
		this.direction=direction;
		this.isLive=true;
		this.type=type;
	}
	
	public Bullet(String info)
	{
		//System.out.println(info);
		this.x=-1;
		this.y=-1;
		this.direction=-1;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		switch(direction)
		{
		case 0:
			moveUp();
			break;
		case 1:
			moveDown();
			break;
		case 2:
			moveLeft();
			break;
		case 3:
			moveRight();
			break;
		}
	}
	
	private boolean hitWall()
	{
		/*
		 * Add boom effects here.
		 */
		int index=Helper.pixelPositionToUnitIndex(x, y);
		Hashtable<Integer,WallUnit> unitTable=MainGame.gameMap.getUnitTable();
		if(unitTable.containsKey(index))
		{
			WallUnit unit=(WallUnit) unitTable.get(index);
			if(unit.isLive())
			{
				unit.isHit();
				return true;
			}
		}
		return false;
	}
	
	private boolean hitTank()
	{
		int index=Helper.pixelPositionToUnitIndex(x, y);
		Vector<Tank> allTanks=MainGame.allTanks;
		for(int t_i=0;t_i<allTanks.size();t_i++)
		{
			Tank tank=allTanks.get(t_i);
			int p2=Helper.coordinateToUnitIndex(tank.position);
			if(p2==index && this.type!=tank.type)
			{
				MainGame.stopGame();
				if(tank.type == 'E')
				{
					MainGame.gameScore+=500;
					//System.out.println("Enemy Tank is Hit, happens at position: "+Helper.coordinateToUnitIndex(tank.position));
				}
				else
				{
					MainGame.gameScore-=500;
					//System.out.println(" Hero Tank is Hit, happens at position: "+Helper.coordinateToUnitIndex(tank.position));
				}
				
				try{
					Hero hero=(Hero)MainGame.allTanks.get(0);
					hero.saveBattleHistory();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.out.println("hero is not in the list, records fails");
				}
				
				Coordinate exposition=Helper.unitIndextoCoordinate(index);
				exposition=Helper.gridToPixelPosition(exposition);
				TankExplosion te=new TankExplosion(exposition);
				MainGame.allBooms.add(te);
				tank.setAlive(false);
				MainGame.gameScore=0;
				MainGame.resumeGame();
				return true;
			}
		}
		
		return false;
	}
	
	private void moveUp()
	{
		while(isLive)
		{
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(hitWall()|| hitTank() || this.y-SPEED<0)
			{
				this.isLive=false;
			}
			else
			{
				this.y-=SPEED;
			}
		}
	}
	
	private void moveDown()
	{
		while(isLive)
		{
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(hitWall()|| hitTank() || this.y+SPEED>MainGame.HEIGHT*MainGame.GRIDHEIGHT)
			{
				this.isLive=false;
			}
			else
			{
				this.y+=SPEED;
			}
		}
	}
	
	private void moveLeft()
	{
		while(isLive)
		{
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(hitWall()|| hitTank() || this.x-SPEED<0)
			{
				this.isLive=false;
			}
			else
			{
				this.x-=SPEED;
			}
		}
	}
	
	private void moveRight()
	{
		while(isLive)
		{
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(hitWall()|| hitTank() || this.x+SPEED>MainGame.WIDTH*MainGame.GRIDWIDTH)
			{
				this.isLive=false;
			}
			else
			{
				this.x+=SPEED;
			}
		}
	}
}

class Tank
{
	protected Coordinate position;
	protected int direction;
    boolean alive;
	char type;
	
	static int SPEED=1;
	final int WIDTH=15;
	final int HEIGHT=13;
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public Tank(int x, int y, int direction)
	{
		this.position=new Coordinate(x,y);
		this.direction=direction;
		alive = true;
	}
	
	public Tank(int x, int y, int direction, char type)
	{
		this.position=new Coordinate(x,y);
		this.direction=direction;
		this.type=type;
	}
	
	public void shotEnemy()
	{
		Coordinate pixelPosition=Helper.gridToPixelPosition(position);
		int x=pixelPosition.x;
		int y=pixelPosition.y;
		Bullet bt=null;
		switch(direction)
		{
		case 0:
			x=x+4+10;
			y=y-3;
			bt=new Bullet(x,y,0,type);
			break;
		case 1:
			x=x+4+10;
			y=y+33;
			bt=new Bullet(x,y,1,type);
			break;
		case 2:
			y=y+4+10;
			x=x-3;
			bt=new Bullet(x,y,2,type);
			break;
		case 3:
			y=y+4+10;
			x=x+33;
			bt=new Bullet(x,y,3,type);
			break;
		}
		MainGame.allBullets.add(bt);
		Thread td=new Thread(bt);
		td.start();
	}
	
	/*
	 * 1, will not go beyond boundary of map.
	 * 2, will not touch the wall.
	 * 3, will not touch any other tanks.
	 */
	public boolean isValidMove(int direction)
	{
		if(this.direction == direction)
		{
			switch(direction)
			{
			case 0:
				if(position.y-1<0)
					return false;
				else
				{
					Coordinate test=new Coordinate(position.x,position.y-1);
					if(hitWall(test) || hitOtherTanks(test))
					{
						return false;
					}
				}
				break;
			case 1:
				if(position.y+1>MainGame.HEIGHT-1)
					return false;
				else
				{
					Coordinate test = new Coordinate(position.x,position.y+1);
					if(hitWall(test)||hitOtherTanks(test))
					{
						return false;
					}
				}
				break;
			case 2:
				if(position.x-1<0)
					return false;
				else
				{
					Coordinate test=new Coordinate(position.x-1,position.y);
					if(hitWall(test)||hitOtherTanks(test))
					{
						return false;
					}
				}
				break;
			case 3:
				if(position.x+1>MainGame.WIDTH-1)
					return false;
				else
				{
					Coordinate test=new Coordinate(position.x+1,position.y);
					if(hitWall(test)||hitOtherTanks(test))
					{
						return false;
					}
				}
				break;
			}
		}
		return true;
	}
	
	private boolean hitOtherTanks(Coordinate co)
	{
		Vector<Tank> allTanks=MainGame.allTanks;
		for(Tank tank:allTanks)
		{
			int p1=Helper.coordinateToUnitIndex(co);
			int p2=Helper.coordinateToUnitIndex(tank.position);
			if(p1==p2)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean hitWall(Coordinate co)
	{
		Hashtable<Integer,WallUnit> gridTable=MainGame.gameMap.getUnitTable();
		int index=Helper.coordinateToUnitIndex(co);
		if(gridTable.containsKey(index))
		{
			WallUnit unit=(WallUnit) gridTable.get(index);
			if(unit.isLive())
				return true;
		}	
		return false;
	}
	
	public void moveUp()
	{
		if (direction == 0)
		{
			position.y=Math.max(position.y-SPEED, 0);
		}else
		{
			direction = 0;
		}
	}
	
	public void moveDown()
	{
		if(direction == 1)
		{
			position.y=Math.min(position.y+SPEED,MainGame.HEIGHT-1);
		}else
		{
			direction=1;
		}
	}
	
	public void moveLeft()
	{
		if(direction == 2)
		{
			position.x=Math.max(position.x-SPEED, 0);
		}else
		{
			direction=2;
		}
	}
	
	public void moveRight()
	{
		if(direction == 3)
		{
			position.x=Math.min(position.x+SPEED,MainGame.WIDTH-1);
		}else
		{
			direction = 3;
		}
	}
}