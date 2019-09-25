package mitw.lobby.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Utils {
	
	public static String locationToStringModes(Location l) {
		return String.valueOf(String.valueOf(String.valueOf(l.getWorld().getName())) + "," + l.getBlockX() + ","
				+ l.getBlockY() + "," + l.getBlockZ() + "," + l.getYaw() + "," + l.getPitch());
	}
	
	public static Location stringToLocModes(String s) {
		Location l = null;
		try {
			World world = Bukkit.getWorld((String) s.split(",")[0]);
			Double x = Double.parseDouble(s.split(",")[1]);
			Double y = Double.parseDouble(s.split(",")[2]);
			Double z = Double.parseDouble(s.split(",")[3]);
			Float yaw = Float.valueOf(Float.parseFloat(s.split(",")[4]));
			Float pitch = Float.valueOf(Float.parseFloat(s.split(",")[5]));
			l = new Location(world, x + 0.5, y + 0.5, z + 0.5, yaw.floatValue(), pitch.floatValue());
		} catch (Exception e) {}
		return l;
	}

}
