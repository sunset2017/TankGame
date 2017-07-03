package TankGame18;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.*;

/*
 * during the interface or arguments, always use grid position.
 * 
 */

public class GamePanel extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String arg0s[])
	{
		GamePanel gp=new GamePanel();

		BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Show Game Panel? Y/N");
		System.out.println("How many enemies in the Game?");
		System.out.println("How many lifes do you want to have? ");
		System.out.println("Choose Game Mode: 1-Recursive Training. 2-Normal Battle");
		System.out.println("          if choosing 1, input how many rounds you want in the next line");
		
		//Game Setting variables.
		boolean showGame=true;
		int enemyNumber=5;
		int heroLives=3;
		boolean recursiveTraining=true;
		int roundsNumber=5000;
		try {
			String input=in.readLine();
			if(input.equals("D"))
			{
				System.out.println("Default Mode");
			}
			else
			{
				if(input.equals("N"))
				    showGame=false;
				enemyNumber=Integer.parseInt(in.readLine());
				heroLives=Integer.parseInt(in.readLine());
				if(in.readLine().equals("1"))
				{
					recursiveTraining=true;
					roundsNumber=Integer.parseInt(in.readLine());
				}
			}
		}catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String mapAddress="/Users/haopan/Desktop/TankGameMap/steelWorld.txt";
		MainGame mg=new MainGame(mapAddress,enemyNumber,heroLives,recursiveTraining,roundsNumber);
		gp.add(mg);
		gp.setSize(MainGame.WIDTH*30,MainGame.HEIGHT*30+22);
		gp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if(showGame)
			gp.setVisible(true);
		Thread td=new Thread(mg);
		td.start();
	}
}

class MainGame extends JPanel implements Runnable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*Map size and unit size constants.
	*/
	final static int HEIGHT=13;
	final static int WIDTH=15;
	final static int GRIDWIDTH=30;
	final static int GRIDHEIGHT=30;
	final static int PACE_LENGTH=180;
	/*Game info variables
	 *
	 */
	static int enemyNumber=0;
	static Map gameMap=null;
	static Vector<Tank> allTanks=new Vector<Tank>();
	static Vector<Bullet> allBullets=new Vector<Bullet>();
	static Vector<BoomEffect> allBooms=new Vector<BoomEffect>();
	static int gameScore;
	static boolean GameStop=false;
	
	//Game Mode.
	boolean RecursiveTrainingMode=false;
	int rounds;
	boolean normalBattle=true;
	
	//Game variables
	private Hero me;
	private int TOTAL_ENEMY;
	private Random rd=new Random();
	
	public MainGame(String address, int enemyNumber, int myLives, boolean recursivetrain, int round)
	{
		MainGame.enemyNumber=enemyNumber;
		TOTAL_ENEMY=enemyNumber;
		gameMap=new Map(address);
		setHeroAlive();
		createEnemies();
		gameScore=0;
		Hero.lives=myLives;
		RecursiveTrainingMode=recursivetrain;
		this.rounds=round;
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		g.fillRect(0, 0,WIDTH*GRIDWIDTH,HEIGHT*GRIDHEIGHT);
		//showCombatInformation(g);
		drawMap(g);
		drawAllTanks(g);
		drawAllBullets(g);
		drawAllBooms(g);
	}
	
	//Random generate Re-birth position.
	
	private boolean checkPositionOccupied(int index)
	{
		Hashtable<Integer,WallUnit> mapUnits=gameMap.getUnitTable();
		if(mapUnits.containsKey(index))
		{
			WallUnit w_u=mapUnits.get(index);
			if(w_u.isLive())
				return true;
		}
		
		for(int i=0;i<allTanks.size();i++)
		{
			Tank tk=allTanks.get(i);
			if(index == Helper.coordinateToUnitIndex(tk.position))
				return true;
		}
		return false;
	}
	
	private void setHeroAlive()
	{
		int posi=rd.nextInt(195);
		while(checkPositionOccupied(posi))
		{
			posi=rd.nextInt(195);
		}
		Coordinate cd=Helper.unitIndextoCoordinate(posi);
		me=new Hero(cd.x,cd.y,0);
		//allTanks.add(me);
		allTanks.add(0,me);
		Thread td=new Thread(me);
		td.start();
	}
	
	private void drawAllBooms(Graphics g)
	{
		for(int i=0;i<allBooms.size();i++)
		{
			BoomEffect bf=allBooms.get(i);
			if(bf.isLive)
			{
				Image ig=bf.showCurrentEffect();
				Coordinate explosionPixelPosition=bf.position;
				g.drawImage(ig,explosionPixelPosition.x,explosionPixelPosition.y,bf.effectSize.x,bf.effectSize.y,this);
				try {
					Thread.sleep(30);
				} catch (InterruptedException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				allBooms.remove(bf);
				i--;
			}
		}
	}
	
	private void drawAllBullets(Graphics g)
	{
		for(int i=0;i<allBullets.size();i++)
		{
			Bullet bt=allBullets.get(i);
			if(bt.getType()=='E')
				g.setColor(Color.YELLOW);
			else
				g.setColor(Color.CYAN);
			if(bt.isLive())
			{
				g.draw3DRect(bt.getX(), bt.getY(), 2, 2, true);
			}else
			{
				allBullets.remove(bt);
				i--;
			}
		}
	}
	
	private void drawAllTanks(Graphics g)
	{	
		for(int i=0;i<allTanks.size();i++)
		{
			Tank tk=allTanks.get(i);
			if(tk.isAlive())
			{
				drawTank(tk,g);
			}else
			{
				allTanks.remove(tk);
				i--;
			}
		}
	}
	
	private void createEnemies()
	{
		for(int i=0;i<enemyNumber;i++)
		{
			RandomEnemy et=new RandomEnemy(2*i,0,1);
			Thread td=new Thread(et);
			td.start();
			allTanks.add(et);
		}
	}
	
	public void drawMap(Graphics g)
	{
		Vector<WallUnit> wall = gameMap.getAllUnits();
		Vector<WallUnit> wasteCollection=new Vector<WallUnit>();
		
		for(int i=0;i<wall.size();i++)
		{
			WallUnit unit=wall.get(i);
			if(unit.isLive())
			{
				Coordinate pixelPosition=Helper.gridToPixelPosition(unit.getPosition());
				if(unit.getType() == 1)
				{
					g.drawImage(WallUnit.hardUnit, pixelPosition.x, pixelPosition.y,GRIDHEIGHT, GRIDWIDTH,this);
				}
				else
				{
					g.drawImage(WallUnit.softUnit, pixelPosition.x, pixelPosition.y,GRIDHEIGHT,GRIDWIDTH,this);
				}
			}else
			{
				wasteCollection.add(unit);
			}
		}
		wall.removeAll(wasteCollection);
	}
	
	public void drawTank(Tank tank, Graphics g)
	{
		/*Tank Size: Length: 30, Width: 21
		 *Direction: 1: up, 2: down, 3: left, 4:right
		 *
		 * */
		if(tank.type == 'E')
			g.setColor(Color.YELLOW);
		else
			g.setColor(Color.CYAN);
		
		int x=tank.position.x*GRIDWIDTH;
		int y=tank.position.y*GRIDHEIGHT;
		
		switch(tank.direction)
		{
		case 0: //up
			x=x+4;
	    	g.fill3DRect(x, y, 5, 30, false);
	    	g.fill3DRect(x+16, y, 5, 30,false);
	    	g.fill3DRect(x+5, y+5,11, 20,false );
	    	g.fillOval(x+6, y+10, 8, 8);
	    	g.drawLine(x+10, y+15, x+10, y-3);  //This line exceeds the size of The tank by 2.
	    	break;
	    case 1: //down
	    	x=x+4;
	    	g.fill3DRect(x, y, 5, 30, false);
	    	g.fill3DRect(x+16, y, 5, 30,false);
	    	g.fill3DRect(x+5, y+5,11, 20,false );
	    	g.fillOval(x+6, y+11, 8, 8);
	    	g.drawLine(x+10, y+15, x+10, y+33);  //This line exceeds the size of The tank by 2.
	    	break;
	    case 2: //left
	    	y=y+4;
	    	g.fill3DRect(x, y, 30, 5, false);
	    	g.fill3DRect(x, y+16, 30, 5, false);
	    	g.fill3DRect(x+5, y+5, 20, 11, false);
	    	g.fillOval(x+10, y+6, 8, 8);
	    	g.drawLine(x+15, y+10, x-3, y+10);
	    	break;
	    case 3: //right
	    	y=y+4;
	    	g.fill3DRect(x, y, 30, 5, false);
	    	g.fill3DRect(x, y+16, 30, 5, false);
	    	g.fill3DRect(x+5, y+5, 20, 11, false);
	    	g.fillOval(x+11, y+6, 8, 8);
	    	g.drawLine(x+15, y+10, x+33, y+10);
	    	break;
		}
	}
	
	
	public static void stopGame()
	{
		MainGame.GameStop=true;
		Tank.SPEED=0;
		Bullet.SPEED=0;
	}
	
	public static void resumeGame()
	{
		MainGame.allBullets.clear();
		MainGame.GameStop=false;
		Tank.SPEED=1;
		Bullet.SPEED=10;
	}
	
	private void recoverTankNumber()
	{
		if(me.isAlive()==false)
		{
			setHeroAlive();
			rounds--;
		}
		
		if(allTanks.size()<TOTAL_ENEMY+1)
		{
			int posi=rd.nextInt(195);
			while(checkPositionOccupied(posi))
			{
				posi=rd.nextInt(195);
			}
			Coordinate cd=Helper.unitIndextoCoordinate(posi);
			while(allTanks.size()<TOTAL_ENEMY+1)
			{
				int num=rd.nextInt(10);
				if(num<5)
				{
				   Enemy tk=new Enemy(cd.x,cd.y,1);
				   allTanks.add(tk);
				   Thread td=new Thread(tk);
				   td.start();
				}
				else
				{
					RandomEnemy tk=new RandomEnemy(cd.x,cd.y,1);
					allTanks.add(tk);
					Thread td=new Thread(tk);
					td.start();
				}
			    System.out.println("Generate new Enemy, current enemy number: "+allTanks.size());
			}
			rounds--;
		}
	}
	
	@Override
	public void run(){
		// TODO Auto-generated method stub
		while(true)
		{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    this.repaint();
		
		if(GameStop==false && RecursiveTrainingMode)
		{
			this.recoverTankNumber();
		}
		
		if(normalBattle)
		{
		    if(me.isAlive()==false && Hero.lives>0)
		    {
			    setHeroAlive();
			    Hero.lives-=1;
		    }
		}
		
		if(RecursiveTrainingMode && this.rounds<=0)
		{
			System.out.println("Training Rounds Ends.");
			break;
		}
		}
	}
}