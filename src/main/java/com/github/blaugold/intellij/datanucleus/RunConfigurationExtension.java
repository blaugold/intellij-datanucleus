package com.github.blaugold.intellij.datanucleus;

import com.google.common.base.Strings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RunConfigurationExtension extends com.intellij.execution.RunConfigurationExtension {

    public static enum API {
        JDO,
        JPA
    }

    private static final Logger log = Logger.getInstance(RunConfigurationExtension.class);
    private static final String PACKAGES_ELEMENT_NAME = "packages";
    private static final String API_ELEMENT_NAME = "api";
    private static final String ENABLED_ELEMENT_NAME = "enabled";

    private String packages;

    public String getPackages() {
        return this.packages;
    }

    public void setPackages(String packages) {
        if (Strings.isNullOrEmpty(packages)) {
            this.packages = null;
            return;
        }

        this.packages = Arrays.stream(packages.split(","))
                .map(String::trim)
                .collect(Collectors.joining());
    }

    private boolean enabled = false;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private API api = API.JDO;

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    @Override
    protected void readExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) {
        packages = element.getChildText(PACKAGES_ELEMENT_NAME);

        enabled = Boolean.valueOf(element.getChildText(ENABLED_ELEMENT_NAME));

        String apiStr = element.getChildText(API_ELEMENT_NAME);
        if (apiStr != null) {
            api = apiStr.equals(API.JDO.toString()) ? API.JDO : API.JPA;
        }
    }

    @Override
    protected void writeExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) {
        if (packages != null) {
            element.addContent(new Element(PACKAGES_ELEMENT_NAME).setText(packages));
        }

        element.addContent(new Element(ENABLED_ELEMENT_NAME).setText(Boolean.toString(enabled)));

        element.addContent(new Element(API_ELEMENT_NAME).setText(api.toString()));
    }

    @Nullable
    @Override
    protected <P extends RunConfigurationBase> SettingsEditor<P> createEditor(@NotNull P configuration) {
        return new ExtentionConfigurable(this);
    }

    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration, JavaParameters params, RunnerSettings runnerSettings) throws ExecutionException {
        if (!enabled) {
            return;
        }

        Module module = ((ModuleBasedConfiguration) configuration).getConfigurationModule().getModule();
        String libraryJar = findLibraryJar(module, "datanucleus-core");

        if (libraryJar != null) {
            setupRuntimeEnhancement(params, libraryJar, getApi());
        }
    }

    private void setupRuntimeEnhancement(JavaParameters params, String libraryJar, API api) {
        StringBuilder enhancerVMParam = new StringBuilder()
                .append("-javaagent:").append(libraryJar)
                .append("=-api=").append(api);

        if (getPackages() != null) {
            enhancerVMParam.append(",").append(getPackages());
        }

        params.getVMParametersList().add(enhancerVMParam.toString());
    }

    private String findLibraryJar(Module module, String libraryName) {
        final String[] libraryJar = {null};

        ModuleRootManager.getInstance(module).orderEntries().forEachLibrary(library -> {
            String curLibraryName = library.getName();
            boolean isDataNucleusCore = curLibraryName != null && curLibraryName.contains(libraryName);
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
