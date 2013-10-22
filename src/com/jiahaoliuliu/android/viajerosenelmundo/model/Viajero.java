package com.jiahaoliuliu.android.viajerosenelmundo.model;

import com.google.android.gms.maps.model.LatLng;

public class Viajero {

	private static final int DEFAULT_ZOOM_LEVEL = 10;
	public enum ChannelId {
		RTVE, CUATRO, TELEMADRID,

		DEFAULT_CHANNEL;
		
		public static ChannelId toChannelId(String channelId) {
			try {
				return valueOf(channelId);
			} catch (Exception exception) {
				return DEFAULT_CHANNEL;
			}
		}
	}

	private String city;
	private String country;
	private LatLng position;
	private int zoomLevel = DEFAULT_ZOOM_LEVEL;
	private ChannelId channel;
	private String url;

	public Viajero() {
		super();
	}

	public Viajero(String city, String country, LatLng position, int zoomLevel, ChannelId channel, String url) {
		super();
		this.city = city;
		this.country = country;
		this.position = position;
		this.zoomLevel = zoomLevel;
		this.channel = channel;
		this.url = url;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public LatLng getPosition() {
		return position;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}

	public int getZoomLevel() {
		return zoomLevel;
	}

	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	public ChannelId getChannel() {
		return channel;
	}

	public void setChannel(ChannelId channel) {
		this.channel = channel;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + zoomLevel;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Viajero other = (Viajero) obj;
		if (channel != other.channel)
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (zoomLevel != other.zoomLevel)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Viajero [city=" + city + ", country=" + country + ", position="
				+ position + ", zoomLevel=" + zoomLevel + ", channel="
				+ channel + ", url=" + url + "]";
	}
}
