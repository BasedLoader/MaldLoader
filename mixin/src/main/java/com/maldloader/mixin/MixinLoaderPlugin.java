package com.maldloader.mixin;

import java.util.List;

import com.maldloader.v0.api.LoaderList;
import com.maldloader.v0.api.classloader.MainClassLoader;
import com.maldloader.v0.api.modloader.ModLoader;
import com.maldloader.v0.api.modloader.ModMetadata;
import com.maldloader.v0.api.plugin.LoaderPlugin;
import com.maldloader.v0.api.transformer.asm.ClassHeader;
import com.maldloader.v0.api.transformer.asm.ClassNodeTransformer;
import com.maldloader.v0.api.transformer.asm.WriterFlagGetter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.service.MixinService;

public class MixinLoaderPlugin implements LoaderPlugin {
	@Override
	public void afterModLoaderInit(LoaderList loader, MainClassLoader classLoader) {
		// todo fork mixin, or reflect into mixin to figure out what classes it actually transforms
		classLoader.addWriterFlags(WriterFlagGetter.StaticAsmFlags.MAXES);
		classLoader.addWriterFlags(WriterFlagGetter.StaticAsmFlags.FRAMES);
		classLoader.addClassNodeTransformer(new ClassNodeTransformer() {
			@Override
			public void accept(ClassNode node) {
				IMixinTransformer transformer = MaldMixinService.service.transformer;
				transformer.transformClass(MixinEnvironment.getDefaultEnvironment(), node.name.replace('/', '.'), node);
			}

			@Override
			public boolean transforms(ClassHeader header) {
				return true;
			}
		});

		MixinService.boot();
		MixinBootstrap.init();
		for(ModLoader<?> modLoader : loader.modLoaders) {
			for(ModMetadata value : modLoader.getMods().values()) {
				if(value instanceof MixinModMetadata) {
					List<String> files = ((MixinModMetadata) value).mixinFiles();
					if(files != null) {
						for(String file : files) {
							Mixins.addConfiguration(file);
						}
					}
				}
			}
		}
	}
}
