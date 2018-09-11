package com.github.blaugold.intellij.datanucleus;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunConfigurationExtension extends com.intellij.execution.RunConfigurationExtension {

    @Override
    protected void readExternal(@NotNull RunConfigurationBase runConfiguration,
                                @NotNull Element element) {
        ExtensionConfig.readExternal(runConfiguration, element);
    }

    @Override
    protected void writeExternal(@NotNull RunConfigurationBase runConfiguration,
                                 @NotNull Element element) {
        ExtensionConfig.writeExternal(runConfiguration, element);
    }

    @Nullable
    @Override
    protected <P extends RunConfigurationBase> SettingsEditor<P> createEditor(@NotNull P configuration) {
        return new ExtensionConfigurable<>(configuration);
    }

    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration,
                                                                      JavaParameters params,
                                                                      RunnerSettings runnerSettings) {
        ExtensionConfig config = ExtensionConfig.get(configuration);
        if (config == null || !config.isEnabled()) {
            return;
        }

        Module
                module =
                ((ModuleBasedConfiguration) configuration).getConfigurationModule().getModule();
        String libraryJar = findLibraryJar(module, "datanucleus-core");

        if (libraryJar != null) {
            setupRuntimeEnhancement(params, libraryJar, config);
        }
    }

    private void setupRuntimeEnhancement(JavaParameters params,
                                         String libraryJar,
                                         ExtensionConfig config) {
        StringBuilder enhancerVMParam = new StringBuilder()
                .append("-javaagent:").append(libraryJar)
                .append("=-api=").append(config.getApi().toString());

        String packages = config.getPackages();
        if (packages != null) {
            enhancerVMParam.append(",").append(packages);
        }

        params.getVMParametersList().add(enhancerVMParam.toString());
    }

    private String findLibraryJar(Module module, String libraryName) {
        final String[] libraryJar = {null};

        ModuleRootManager.getInstance(module).orderEntries().forEachLibrary(library -> {
            String curLibraryName = library.getName();
            boolean
                    isDataNucleusCore =
                    curLibraryName != null && curLibraryName.contains(libraryName);
            if (isDataNucleusCore) {
                String jarUrl = library.getUrls(OrderRootType.CLASSES)[0];
                libraryJar[0] = jarUrl
                        .replaceFirst("^jar://", "")
                        .replaceFirst("!/$", "");
                return false;
            }
            return true;
        });

        return libraryJar[0];
    }

    @Override
    protected boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        if (configuration instanceof ModuleBasedConfiguration) {
            if (((ModuleBasedConfiguration) configuration).getConfigurationModule() instanceof JavaRunConfigurationModule) {
                return true;
            }
        }
        return true;
    }

    @Nullable
    @Override
    protected String getEditorTitle() {
        return "DataNucleus Enhancer";
    }

}
