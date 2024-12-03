/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.reporting;

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.file.FileFactory;
import org.gradle.api.internal.file.FileLookup;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;
import org.gradle.internal.instrumentation.api.annotations.BytecodeUpgrade;
import org.gradle.internal.instrumentation.api.annotations.ReplacesEagerProperty;

import javax.inject.Inject;
import java.io.File;

/**
 * A project extension named "reporting" that provides basic reporting settings and utilities.
 * <p>
 * Example usage:
 * <pre>
 * reporting {
 *     baseDir "$buildDir/our-reports"
 * }
 * </pre>
 * <p>
 * When implementing a task that produces reports, the location of where to generate reports should be obtained
 * via the {@link #file(String)} method of this extension.
 */
public abstract class ReportingExtension {

    /**
     * The name of this extension ("{@value}")
     */
    public static final String NAME = "reporting";

    /**
     * The default name of the base directory for all reports, relative to {@link ProjectLayout#getBuildDirectory()} ({@value}).
     */
    public static final String DEFAULT_REPORTS_DIR_NAME = "reports";

    private final ProjectInternal project;
    private final ExtensiblePolymorphicDomainObjectContainer<ReportSpec> reports;

    @Inject
    public ReportingExtension(Project project) {
        this.project = (ProjectInternal)project;
        this.reports = project.getObjects().polymorphicDomainObjectContainer(ReportSpec.class);
        getBaseDirectory().convention(project.getLayout().getBuildDirectory().dir(DEFAULT_REPORTS_DIR_NAME));
    }

    /**
     * The base directory property to use for all reports.
     *
     * @since 4.4
     */
    @ReplacesEagerProperty(adapter = BaseDirAdapter.class)
    public abstract DirectoryProperty getBaseDirectory();

    /**
     * Creates a file object for the given path, relative to {@link #getBaseDirectory()}.
     * <p>
     * The reporting base dir can be changed, so users of this method should use it on demand where appropriate.
     *
     * @param path the relative path
     * @return a file object at the given path relative to {@link #getBaseDirectory()}
     */
    public File file(String path) {  // TODO should this take Object?
        return this.project.getServices().get(FileLookup.class).getFileResolver(getBaseDirectory().getAsFile().get()).resolve(path);
    }

    // TODO this doesn't belong here, that java plugin should add an extension to this guy with this
    @ReplacesEagerProperty
    public Provider<String> getApiDocTitle() {
        Object version = project.getVersion();
        if (Project.DEFAULT_VERSION.equals(version)) {
            return Providers.of(project.getName() + " API");
        } else {
            return Providers.of(project.getName() + " " + version + " API");
        }
    }

    /**
     * Container for aggregation reports, which may be configured automatically in reaction to the presence of the jvm-test-suite plugin.
     *
     * @return A container of known aggregation reports
     * @since 7.4
     */
    @Incubating
    public ExtensiblePolymorphicDomainObjectContainer<ReportSpec> getReports() {
        return reports;
    }

    static class BaseDirAdapter {
        @BytecodeUpgrade
        static File getBaseDir(ReportingExtension reportingExtension) {
            return reportingExtension.getBaseDirectory().getAsFile().get();
        }

        @BytecodeUpgrade
        static void setBaseDir(ReportingExtension reportingExtension, File baseDir) {
            reportingExtension.getBaseDirectory().set(baseDir);
        }

        @BytecodeUpgrade
        static void setBaseDir(ReportingExtension reportingExtension, Object baseDir) {
            reportingExtension.getBaseDirectory().set(
                reportingExtension.project
                    .provider(() -> reportingExtension.project.file(baseDir))
                    .map(dir -> reportingExtension.project.getServices().get(FileFactory.class).dir(dir))
            );
        }
    }
}
