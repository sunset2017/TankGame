package TankGame18;
import java.util.*;

import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.io.*;

/*  type: 1: hard square. 2: softsquare
 *  
 */

class WallUnit
{
	private Coordinate position;
	private int type;
	private boolean isLive;
	public static Image hardUnit = Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/hardwall.png"));
	public static Image softUnit = Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/softwall.png"));
	
	public Coordinate getPosition() {
		return position;
	}

	public void setPosition(Coordinate position) {
		this.position = position;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isLive() {
		return isLive;
	}

	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}
	
	public WallUnit(Coordinate position, int type)
	{
		this.position=position;
		this.type=type;
		this.isLive=true;
	}
	
	public void isHit()
	{
		if(type == 2)
		{
			isLive=false;
		}
	}
}

class Map
{
	private Vector<WallUnit> allUnits = new Vector<WallUnit>();
	
	//This Hashtable maps unitIndex to the corresponding coordinate of the wall using method in Helper class.
	private Hashtable<Integer,WallUnit> unitTable=new Hashtable<Integer,WallUnit>();
	
	public Hashtable<Integer,WallUnit> getUnitTable()
	{
		return unitTable;
	}
	
	public Vector<WallUnit> getAllUnits()
	{
		return allUnits;
	}
	
	public Map(String address)
	{
		File f=new File(address);
		this.readMap(f);
	}
	
	private void readMap(File f)
	{
		if(f == null)
		{
			System.out.println("invalid map file");
			return;
		}
		try
		{
		FileReader fr=new FileReader(f);
		BufferedReader bf=new BufferedReader(fr);
		String line=bf.readLine();
		int type=2;
		if(line ==null)
		{
			bf.close();
			fr.close();
			return;
		}
		
		if(line.equals("h"))
		{
			type=1;
		}
		while((line=bf.readLine())!= null)
		{
			//System.out.println(line+" line out put");
			if(line.equals("h"))
			{
				type=1;
				continue;
			}
			int left=0;
			for(int i=0; i<line.length();i++)
			{
				if(line.charAt(i) == ',')
				{
					int index=Integer.parseInt(line.substring(left, i));
					//System.out.println(index);
					left = i+1;
					Coordinate co=Helper.unitIndextoCoordinate(index);
					WallUnit unit=new WallUnit(co,type);
					unitTable.put(index,unit);
					allUnits.add(unit);
				}
			}
		}
		bf.close();
		fr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

class Helper
{
	/*
	 * Coordintae Transformation.
	 * one dimensional unit index to two dimensional grid position coordinates and vice versa
	 */
	public static Coordinate unitIndextoCoordinate(int number)
	{
		int x=number%MainGame.WIDTH;
		int y=number/MainGame.WIDTH;
		Coordinate co=new Coordinate(x,y);
		return co;
	}
	
	public static int coordinateToUnitIndex(Coordinate co)
	{
		return co.y*MainGame.WIDTH+co.x;
	}
	
	public static Coordinate gridToPixelPosition(Coordinate co)
	{
		return new Coordinate(co.x*MainGame.GRIDWIDTH,co.y*MainGame.GRIDHEIGHT);
	}
	
	public static int pixelPositionToUnitIndex(int x, int y)
	{
		int rows=y/30;
		int cols=x/30;
		return rows*15+cols;
	}
	
	public static int getManhattanDistance(Coordinate a, Coordinate b)
	{
		int distance=Math.abs(a.x-b.x)+Math.abs(a.y-b.y);
		return distance;
	}
	
	public static Coordinate pixelPositiontoGridPosition(int x, int y)
	{
		int value=Helper.pixelPositionToUnitIndex(x, y);
		Coordinate nc=Helper.unitIndextoCoordinate(value);
		return nc;
	}
	
	public static void printContainer(Vector<Integer> v)
	{
		for(int i: v)
		{
			System.out.print(i+", ");
		}
		System.out.println("");
	}
	
	public static void printContainer(List<Integer> v)
	{
		for(int i: v)
		{
			System.out.print(i+", ");
		}
		System.out.println("");
	}
}

