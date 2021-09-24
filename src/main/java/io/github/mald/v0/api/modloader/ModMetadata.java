package io.github.mald.v0.api.modloader;

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

    //TODO: represent dependencies

    /**
     * The path to the mixin configuration file
     */
    String mixinFile();
}
