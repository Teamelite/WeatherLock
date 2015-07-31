package mfungamer.plugin.weatherlock;

import java.util.ArrayList;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.weather.WeatherChangeEvent;


public class EventListener implements Listener{
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent ev){
		Boolean check = false;
		for(String[] s : Main.LockWeatherList){
			World w = Main.instance.getServer().getWorld(s[0]);
			if(w.equals(ev.getWorld())){
				check = true;
				for(ArrayList<String> list : Main.CustomWeatherList){
					if(list.get(0).equalsIgnoreCase(ev.getWorld().getName())){
						switch(list.get(1)){
						case "0":
							check = false;
							break;
						case "1":
							Main.CustomWeatherList.remove(list);
							String weather = "sun";
							for(String[] al : Main.LockWeatherList){
								if(al[0].equals(w.getName())){
									weather=al[1];
								}
							}
							Main.setupWeather(ev.getWorld(),weather);
							break;
						}
						break;
					}
				}
				break;
			}
		}
		if(check){
			ev.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent ev){
		try{
			if(ev.getPlayer().hasPermission("customweather.info")){
				Main.weathersee(ev.getPlayer(), ev.getPlayer().getWorld());
			}
		} catch(NullPointerException e){}
	}
}
