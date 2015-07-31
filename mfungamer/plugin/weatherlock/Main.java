package mfungamer.plugin.weatherlock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	public static String url = "SQL-URL";
	public static String user = "user";
	public static String password = "password";
	public static Main instance;
	public static ArrayList<String[]> LockWeatherList = new ArrayList<String[]>();
	public static ArrayList<ArrayList<String>> CustomWeatherList = new ArrayList<ArrayList<String>>();
	
	public static Boolean setupWeather(World w, String weather){
		try{
			ArrayList<String> list = new ArrayList<String>();
			list.add(w.getName());
			list.add("0");
			CustomWeatherList.add(list);
			int duration = 1000000;
			switch(weather){
			case "sun":
				w.setStorm(false);
				w.setThundering(false);
				w.setWeatherDuration(duration*20);
				CustomWeatherList.remove(list);
				return true;
			case "rain":
				w.setStorm(true);
				w.setThundering(false);
				w.setWeatherDuration(duration*20);
				CustomWeatherList.remove(list);
				return true;
			case "storm":
				w.setStorm(true);
				w.setThundering(true);
				w.setWeatherDuration(duration*20);
				CustomWeatherList.remove(list);
				return true;
			default:
				CustomWeatherList.remove(list);
				return false;
			}
		} catch(Exception e){
			return false;
		}
	}
	
	public static Boolean unlock(String world){
		try{
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stmt = con.createStatement();
			String query = "DELETE FROM WeatherLock WHERE World="+world;
			stmt.execute(query);
			stmt.close();
			con.close();
			return true;
		} catch(SQLException e){
			e.printStackTrace();
			System.out.println("SQL-State: "+e.getSQLState());
			System.out.println("Vendor-Error: "+e.getErrorCode());
			System.out.println("SQL-Error: "+e.getMessage());
			return false;
		}
	}
	
	public static void weathersee(Player p,World w) throws NullPointerException{
		Boolean check = true;
		for(ArrayList<String> al : CustomWeatherList){
			if(al.get(0).equalsIgnoreCase(w.getName())){
				String weather = null;
				if(!w.hasStorm()&&!w.isThundering()){
					weather = "sun";
				} else if(w.hasStorm()&&!w.isThundering()){
					weather = "rain";
				} else if(w.hasStorm()&&w.isThundering()){
					weather = "storm";
				}
				p.sendMessage("[Custom-Weather] The weather in the world is customly set to "+weather+" for another "+String.valueOf(w.getWeatherDuration()/20)+" seconds.");
				check = false;
				break;
			}
		}
		if(check){
			for(String[] sa : LockWeatherList){
				if(sa[0].equalsIgnoreCase(w.getName())){
					p.sendMessage("The weather in this world is locked to "+sa[1]+".");
					break;
				}
			}
		}
	}
	
	public static void weathersee(ConsoleCommandSender p,World w) throws NullPointerException{
		Boolean check = true;
		for(ArrayList<String> al : CustomWeatherList){
			if(al.get(0).equalsIgnoreCase(w.getName())){
				String weather = null;
				if(!w.hasStorm()&&!w.isThundering()){
					weather = "sun";
				} else if(w.hasStorm()&&!w.isThundering()){
					weather = "rain";
				} else if(w.hasStorm()&&w.isThundering()){
					weather = "storm";
				}
				p.sendMessage("[Custom-Weather] The weather in the world is customly set to "+weather+" for another "+String.valueOf(w.getWeatherDuration()/20)+" seconds.");
				check = false;
				break;
			}
		}
		if(check){
			for(String[] sa : LockWeatherList){
				if(sa[0].equalsIgnoreCase(w.getName())){
					p.sendMessage("The weather in this world is locked to "+sa[1]+".");
					break;
				}
			}
		}
	}
	
	@Override
	public void onEnable() {
		instance = this;
		try{
			Connection con = DriverManager.getConnection(url, user, password);
			Statement stmt = con.createStatement();
			String query = "SELECT * FROM WeatherLock";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				World world = this.getServer().getWorld(rs.getString("World"));
				LockWeatherList.add(new String[]{world.getName(),rs.getString("Weather")});
				Boolean b = setupWeather(world,rs.getString("Weather"));
				if(!b){
					System.out.println(ChatColor.DARK_RED+"[Custom-Weather] An error occured while initalising the "+String.valueOf(LockWeatherList.size())+". weather. The weather will be unlocked.");
					String[] sa = LockWeatherList.get(LockWeatherList.size()-1);
					Boolean b2 = unlock(sa[0]);
					if(!b2){
						System.out.println(ChatColor.DARK_RED+"[Custom-Weather] An error occured while deleting the weather-lock. The deletion could not be completed.");
					} else{
						LockWeatherList.remove(sa);
					}
				}
			}
			rs.close();
			stmt.close();
			con.close();
			World w = this.getServer().getWorld("world");
			LockWeatherList.add(new String[]{w.getName(),"sun"});
			setupWeather(w, "sun");
			this.getServer().getPluginManager().registerEvents(new EventListener(), this);
		} catch(SQLException e){
			e.printStackTrace();
			System.out.println("SQL-State: "+e.getSQLState());
			System.out.println("Vendor-Error: "+e.getErrorCode());
			System.out.println("SQL-Error: "+e.getMessage());
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("custom-weather")){
			if(sender instanceof Player){
				if(!sender.hasPermission("customweather.custom")){
					sender.sendMessage("You do not have the permission to use this command!");
					return true;
				}
			}
			String weather = null;
			int duration = 0;
			World world = null;
			if(sender instanceof Player){
				if(args.length==1){
					weather = args[0].toLowerCase();
					duration = 120;
					world = ((Player)sender).getWorld();
				} else if(args.length==2){
					weather = args[0].toLowerCase(); 
					duration = Integer.parseInt(args[1]);
					world = ((Player)sender).getWorld();
				} else if(args.length>2){
					weather = args[0].toLowerCase(); 
					duration = Integer.parseInt(args[1]);
					world = this.getServer().getWorld(args[2]);
				} else{
					return false;
				}
			} else if(sender instanceof ConsoleCommandSender){
				if(args.length==2){
					weather = args[0].toLowerCase();
					duration = 120;
					world = this.getServer().getWorld(args[1]);
				} else if(args.length>=3){
					weather = args[0].toLowerCase(); 
					duration = Integer.parseInt(args[1]);
					world = this.getServer().getWorld(args[2]);
				} else{
					return false;
				}
				
			} else{
				return false;
			}
			
			for(ArrayList<String> al : CustomWeatherList){
				if(al.get(0).equals(world.getName())){
					CustomWeatherList.remove(al);
					break;
				}
			}
			
			ArrayList<String> list = new ArrayList<String>();
			Integer i = CustomWeatherList.size();
			list.add(world.getName());
			list.add("0");
			CustomWeatherList.add(list);
			switch(weather){
			case "sun":
				world.setStorm(false);
				world.setThundering(false);
				break;
			case "rain":
				world.setStorm(true);
				world.setThundering(false);
				break;
			case "storm":
				world.setStorm(true);
				world.setThundering(true);
				break;
			default:
				sender.sendMessage("Weather modes:\n- sun\n- rain\n- storm");
				return false;
			}
			world.setWeatherDuration(duration*20);
			CustomWeatherList.get(i).set(1, "1");
			return true;
		}
		
		
		
		if(cmd.getName().equalsIgnoreCase("lock-weather")){
			if(sender instanceof Player){
				if(!sender.hasPermission("customweather.lock")){
					sender.sendMessage("You do not have the permission to use this command!");
					return true;
				}
			}
			World w = null;
			String weather = null;
			if(args.length==0 && sender instanceof Player){
					w = ((Player)sender).getWorld();
					if(!w.hasStorm()&&!w.isThundering()){
						weather = "sun";
					} else if(w.hasStorm()&&!w.isThundering()){
						weather = "rain";
					} else if(w.hasStorm()&&w.isThundering()){
						weather = "storm";
					}
			}
			else if (args.length==1 && sender instanceof Player){
					w = ((Player)sender).getWorld();
					weather = args[0].toLowerCase();
			} else if(args.length>=2){
					w = this.getServer().getWorld(args[1]);
					weather = args[0].toLowerCase();
			}
			if(w==null){
				sender.sendMessage(ChatColor.RED+"The world does not exist!");
				return false;
			}
			String[] searched = null;
			for(String[] sa : LockWeatherList){
				if(sa[0].equalsIgnoreCase(w.getName())){
					searched = sa;
				}
			}
			if(searched!=null){
				sender.sendMessage("The weather in the given world is already locked.");
				return true;
			}
			if(setupWeather(w, weather)){
				try{
					Connection con = DriverManager.getConnection(url, user, password);
					Statement stmt = con.createStatement();
					String query = "INSERT INTO WeatherLock(World,Weather) VALUES("+w.getName()+","+weather+")";
					stmt.execute(query);
					stmt.close();
					con.close();
					sender.sendMessage("The weather has been locked!");
					return true;
				} catch (SQLException e){
					e.printStackTrace();
					System.out.println("SQL-State: "+e.getSQLState());
					System.out.println("Vendor-Error: "+e.getErrorCode());
					System.out.println("SQL-Error: "+e.getMessage());
					return false;
				}
			} else{
				sender.sendMessage(ChatColor.RED+"An error occured!");
				return false;
			}
		}
		
		
		
		if(cmd.getName().equalsIgnoreCase("unlock-weather")){
			if(sender instanceof Player){
				if(!sender.hasPermission("customweather.unlock")){
					sender.sendMessage("You do not have the permission to use this command!");
					return true;
				}
			}
			World w = null;
			if(args.length==0 && sender instanceof Player){
				w = ((Player)sender).getWorld();
			} else if(args.length>=1){
				w = this.getServer().getWorld(args[0]);
			} else{
				return false;
			}
			if(w==null){
				sender.sendMessage(ChatColor.RED+"The world does not exist!");
				return false;
			} else{
				String[] searched = null;
				for(String[] sa : LockWeatherList){
					if(sa[0].equalsIgnoreCase(w.getName())){
						searched = sa;
						break;
					}
				}
				if(searched==null){
					sender.sendMessage("The weather in the given world is not locked!");
					return true;
				} else{
					Boolean b = unlock(w.getName());
					if(!b){
						sender.sendMessage("An error occured whilst unlocking the weather!");
						return false;
					} else{
						LockWeatherList.remove(searched);
						w.setWeatherDuration(60*20);
						sender.sendMessage("The wetaher has been succesfully unlocked!");
						return true;
					}
				}
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("info-weather")){
			if(sender instanceof Player){
				if(!sender.hasPermission("customweather.lock")){
					sender.sendMessage("You do not have the permission to use this command!");
					return true;
				}
			}
			if(sender instanceof ConsoleCommandSender){
				if(args.length>=1){
					World w = null;
					w = this.getServer().getWorld(args[0]);
					try{
						weathersee((ConsoleCommandSender)sender, w);
						return true;
					} catch(NullPointerException e){
						sender.sendMessage(ChatColor.RED+"The world does not exist!");
						return false;
					}
				} else{
					return false;
				}
			} else if(sender instanceof Player){
				World w = null;
				if(args.length>=1){
					w = this.getServer().getWorld(args[0]);
				} else{
					w = ((Player)sender).getWorld();
				}
				try{
					weathersee((Player) sender, w);
					return true;
				} catch(NullPointerException e){
					sender.sendMessage(ChatColor.RED+"The world does not exist!");
					return false;
				}
			}
		}
		return false;
	}

}
