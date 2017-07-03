package TankGame18;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

class Hero extends Tank implements Runnable
{
	//Game Information.
	Vector<Tank> alltanks=MainGame.allTanks;
	Vector<Bullet> allbullets=MainGame.allBullets;
	Map gameMap=MainGame.gameMap;
	static int lives;
	private final static int ALERT=-100;
	
	// Game Variables.
	private int count=0;
	private Coordinate targetPosition=null;
	boolean fireEnemy=false;
	private Vector<Integer> goalPath;
	
	//Information Recorder.
	HistoryRecorder hr;
	
	public Hero(int x, int y, int direction){
		super(x, y, direction);
		// TODO Auto-generated constructor stub
		type = 'H';
		goalPath=new Vector<Integer>();
		hr=new HistoryRecorder();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(this.alive)
		{	
			try {
				Thread.sleep(MainGame.PACE_LENGTH);
			}catch (InterruptedException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Command cd=getCommand();
			if(cd.fireEnemy)
			{
				this.shotEnemy();
			}
			switch(cd.direction)
			{
			case 0:
				if(isValidMove(0))
				{
					this.moveUp();
				}
				break;
			case 1:
				if(isValidMove(1))
				{
					this.moveDown();
				}
				break;
			case 2:
				if(isValidMove(2))
				{
					this.moveLeft();
				}
				break;
			case 3:
				if(isValidMove(3))
				{
					this.moveRight();
				}
			}
		//Record battle history when hero is alive.
		if(MainGame.GameStop==false)
			hr.addBattleHistory();
		}
	}
	
	public void saveBattleHistory()
	{
		hr.addBattleHistory();
		hr.calculateMyUtility(MainGame.gameScore);
		hr.saveToCSVfile();
	}
	public Command getCommand()
	{
		Command cd=new Command();
		Vector<Integer> validMove=getAvailableMove(this.position);
		int BFSdirecting=getNextMoveinBFS();
		int value=Integer.MIN_VALUE;
		cd.direction=0;
		for(int move:validMove)
		{
			int tmp=getMoveHeuristic(move);
			if(move == BFSdirecting)
			{
				tmp+=90;
			}
			
			if(tmp>value)
			{
				cd.direction=move;
				value=tmp;
			}
		}
		cd.fireEnemy=fireEnemy;
		return cd;
	}
	
	private int getNextMoveinBFS()
	{
		Coordinate posi=alltanks.get(alltanks.size()-1).position;
		if(targetPosition==null)
			targetPosition=posi;
		int distance=Helper.getManhattanDistance(targetPosition, posi);
		if(goalPath.size()<=count || distance>5)
		{
			targetPosition=posi;
			goalPath=getMovePath(this.position,targetPosition);
			count = 0;
		}
		int direction= goalPath.get(count);
		count++;
		return direction;
	}
	
	private int getMoveHeuristic(int move)
	{
		int value=0;
		if(this.direction!=move)
		{
			//The position doesn't change. return the value of current position.
			int tmp=checkEnemyInSameLine(this.position,move);
			value+=tmp;
		}else
		{
			Coordinate nextPosi=getNextPosition(this.position,move);
			int tmp=checkEnemyInSameLine(nextPosi,move);
			value+=tmp;
		}
		return value;
	}
	
	private int checkEnemyInSameLine(Coordinate position, int move)
	{
		int value=0;
		for(int i=1;i<alltanks.size();i++)
		{
			Tank emy=alltanks.get(i);
			Coordinate EnemyPosi = alltanks.get(i).position;
			if(position.x==EnemyPosi.x || position.y==EnemyPosi.y)
			{
				if(position.x==EnemyPosi.x)
				{
					if(position.y<EnemyPosi.y)
					{
						if(emy.direction==0)
						    value+=ALERT;
						else if(move==1)
						{
							value-=ALERT*0.3;
							fireEnemy=true;
						}
					}else
					{
						if(emy.direction==1)
							value+=ALERT;
						else if(move==0)
						{
							value-=ALERT*0.3;
							fireEnemy=true;
						}
					}
				}
				else
				{
					if(position.x<EnemyPosi.x)
					{
						if(emy.direction == 2)
							value+=ALERT;
						else if(move==3)
						{
							value-=ALERT*0.3;
							fireEnemy=true;
						}
					}else
					{
						if(emy.direction==3)
							value+=ALERT;
						else if(move==2)
						{
							value-=ALERT*0.3;
							fireEnemy=true;
						}
					}
				}
			}
		}	
		return value;
	}
	
	//return the direction by BFS, from ca to cb.
	private Vector<Integer> getMovePath(Coordinate ca,Coordinate cb)
	{
		PathCoordinate first=new PathCoordinate(ca.x,ca.y);
		PathCoordinate last=new PathCoordinate(cb.x,cb.y);

		Vector<Integer> goalPath=new Vector<Integer>();
		if(ca.x==cb.x && ca.y==cb.y)
		{
			goalPath.add(4);
			return goalPath;
		}
		
		Queue<Coordinate> Path=new LinkedList<Coordinate>();
		Set<Integer> visitedNode=new HashSet<Integer>();
		Path.add(first);
		visitedNode.add(Helper.coordinateToUnitIndex(first));
		
		while(Path.size()>0)
		{
			PathCoordinate tmpNode=(PathCoordinate) Path.poll();
			visitedNode.add(Helper.coordinateToUnitIndex(tmpNode));
			if(tmpNode.x == last.x && tmpNode.y==last.y)
			{
				goalPath=tmpNode.getCurrentMoves();
				//printVector(goalPath);
				return goalPath;
			}
			else
			{
				Vector<Integer> allMoves=getAvailableMove(tmpNode);
				Vector<Integer> currentMove=tmpNode.getCurrentMoves();
				for(int i:allMoves)
				{
					PathCoordinate nextPosition=getNextPosition(tmpNode, i);
					int kkk=Helper.coordinateToUnitIndex(nextPosition);
					if(!visitedNode.contains(kkk))
					{
						Vector<Integer> tmpMove=new Vector<Integer>(currentMove);
						if(currentMove.size()>0 && currentMove.get(currentMove.size()-1) != i)
							tmpMove.add(currentMove.get(currentMove.size()-1));
						tmpMove.add(i);
						nextPosition.setCurrentMoves(tmpMove);
						Path.add(nextPosition);
						
					}
				}
			}
		}
		System.out.println("No path found");
		return goalPath;
	}
	
	//get available move in each position.
	private Vector<Integer> getAvailableMove(Coordinate co)
	{
		Vector<Integer> allMoves=new Vector<Integer>();
		Hashtable<Integer,WallUnit> wallRecord=gameMap.getUnitTable();
		
		if(co.y-1>=0)
		{
			int index=Helper.coordinateToUnitIndex(new Coordinate(co.x,co.y-1));
			if(!wallRecord.containsKey(index))
			{
				allMoves.add(0);
			}
			else
			{
				WallUnit unit=wallRecord.get(index);
				if(unit.isLive()==false)
				{
					allMoves.add(0);
				}
			}
		}
		
		if(co.y+1<MainGame.HEIGHT)
		{
			int index=Helper.coordinateToUnitIndex(new Coordinate(co.x,co.y+1));
			if(!wallRecord.containsKey(index))
			{
				allMoves.add(1);
			}else
			{
				WallUnit unit=wallRecord.get(index);
				if(unit.isLive()==false)
				{
					allMoves.add(1);
				}
			}
		}
		
		if(co.x-1>=0)
		{
			int index=Helper.coordinateToUnitIndex(new Coordinate(co.x-1,co.y));
			if(!wallRecord.containsKey(index))
			{
				allMoves.add(2);
			}else
			{
				WallUnit unit=wallRecord.get(index);
				if(unit.isLive()==false)
				{
					allMoves.add(2);
				}
			}
		}
		
		if(co.x+1<MainGame.WIDTH)
		{
			int index=Helper.coordinateToUnitIndex(new Coordinate(co.x+1,co.y));
			if(!wallRecord.containsKey(index))
			{
				allMoves.add(3);
			}else
			{
				WallUnit unit=wallRecord.get(index);
				if(unit.isLive()==false)
				{
					allMoves.add(3);
				}
			}
		}
		return allMoves;
	}
	
	private PathCoordinate getNextPosition(Coordinate co, int direction)
	{
		PathCoordinate newCo=null;
		switch(direction)
		{
		case 0:
			newCo=new PathCoordinate(co.x,co.y-1);
			return newCo;
		case 1:
			newCo=new PathCoordinate(co.x,co.y+1);
			return newCo;
		case 2:
			newCo=new PathCoordinate(co.x-1,co.y);
			return newCo;
		case 3:
			newCo=new PathCoordinate(co.x+1,co.y);
			return newCo;
		case 4:
			return new PathCoordinate(co.x,co.y);
		}
		return newCo;
	}
}

class PathCoordinate extends Coordinate
{
	private Vector<Integer> currentMoves=new Vector<Integer>();
	
	public Vector<Integer> getCurrentMoves() {
		return currentMoves;
	}
	
	public void setCurrentMoves(Vector<Integer> Moves) {
		currentMoves=new Vector<Integer>(Moves);
	}
	
	public PathCoordinate(int x, int y){
		super(x, y);
		// TODO Auto-generated constructor stub
	}
	
}


/*
 * Game Information Variables
 * 'alltanks' has all the living tanks currently in the battle, the first one in the Vector is yourself. 
 * 'allbullets' has all the bullets flying in the field.
 * 
 * Submit this file for Competition!
 * 
 * In each step, you need to generate a 'Command Variable' (See Command class in 'TankGameClass'), 
 * telling the agent where to move and whether needs to fire.
 * 
 * direction is denoted by 0,1,2,3 where: 
 * 
 * 0: up. 
 * 1: down. 
 * 2: left. 
 * 3: right.
 * 4: stay.
 *
 * Your scores on GameBoard can be accessed by calling: MainGame.gameScore.
 *
 * Integer 'lives' tells how many lives left for you. Starts from 3.
 * Coordinate position gives the current position.
 * 
 */

