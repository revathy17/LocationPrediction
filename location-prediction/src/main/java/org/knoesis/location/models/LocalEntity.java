package org.knoesis.location.models;

public class LocalEntity {

		private String localEntity;
		private String location;
		private double score;
		
		public LocalEntity(String localEntity, String location, double score) {
			this.localEntity = localEntity;
			this.location = location;
			this.score = score;
		}
		
		public String getLocalEntity() {
			return this.localEntity;
		}
		
		public String getLocation() {
			return this.location;
		}
		
		public double getScore() {
			return this.score;
		}

}
