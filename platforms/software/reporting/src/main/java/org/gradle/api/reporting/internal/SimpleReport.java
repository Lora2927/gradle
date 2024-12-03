/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.api.reporting.internal;

import groovy.lang.Closure;
import org.gradle.api.Describable;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemLocationProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.util.internal.ConfigureUtil;

import java.io.File;

public abstract class SimpleReport implements ConfigurableReport {

    public SimpleReport(String name, Describable displayName, OutputType outputType) {
        this.getName().set(name);
        this.getDisplayName().set(displayName.getDisplayName());
        this.getOutputType().set(outputType);
    }

    @Override
    public abstract Property<String> getName();

    @Override
    public abstract Property<String> getDisplayName();

    @Override
    public String toString() {
        return "Report " + getName().get();
    }

    @Override
    public abstract FileSystemLocationProperty<? extends FileSystemLocation> getOutputLocation();

    @Deprecated
    @Override
    public void setDestination(File file) {
        DeprecationLogger.deprecateProperty(Report.class, "destination")
                .replaceWith("outputLocation")
                .willBeRemovedInGradle9()
                .withDslReference()
                .nagUser();

        getOutputLocation().fileValue(file);
    }
    @Override
    public abstract Property<OutputType> getOutputType();

    @Override
    public Report configure(Closure configure) {
        return ConfigureUtil.configureSelf(configure, this);
    }
}
