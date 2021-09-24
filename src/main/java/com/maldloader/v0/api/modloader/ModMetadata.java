package com.maldloader.v0.api.modloader;

import java.util.Map;

/**
 * Represents Metadata of a mod such as ID, Name, DisplayName, Dependencies, etc
 */
public interface ModMetadata {

    /**
     * The Identifier of the mod
     */
	String id();

	/**
	 * The name of the mod
	 */
	String name();

	/**
	 * The description of the mod
	 */
	String description();

	class Standard implements ModMetadata {
		String id, licence, version;
		String name;
		String[] authors;
		String description;
		Map<String, String> urls;

		public String getName() {
			return this.name;
		}

		public String getId() {
			return this.id;
		}

		public String getLicence() {
			return this.licence;
		}

		public String getVersion() {
			return this.version;
		}


		public String[] getAuthors() {
			return this.authors;
		}

		public Map<String, String> getUrls() {
			return this.urls;
		}

		@Override
		public String id() {
			return this.getId();
		}

		@Override
		public String name() {
			return this.name;
		}

		@Override
		public String description() {
			return this.description;
		}
	}
}
