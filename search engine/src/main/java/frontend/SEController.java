package frontend;

import java.util.Calendar;

public class SEController{
	String ip_port;

	long lastAccessTime = 0;
	boolean isAlive;

	public SEController(String s) {
		lastAccessTime = Calendar.getInstance().getTimeInMillis();
		ip_port = s;
	}

	public boolean checkAlive() {
		return lastAccessTime + 5000 >= Calendar.getInstance().getTimeInMillis();
	}

	public String getIp_port() {
		return ip_port;
	}

	public void setIp_port(String ip_port) {
		this.ip_port = ip_port;
	}

	public void setLastAccessTime() {

		this.lastAccessTime = Calendar.getInstance().getTimeInMillis();
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

}