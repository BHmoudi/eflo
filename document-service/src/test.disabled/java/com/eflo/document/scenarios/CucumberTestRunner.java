package com.eflo.document.scenarios;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber BDD Test Runner
 *
 * Executes all feature files found in the classpath under scenarios/ directory.
 * Uses JUnit Platform Suite to run Cucumber tests.
 *
 * Configuration:
 * - Feature files location: src/test/resources/scenarios/
 * - Step definitions package: com.eflo.document.scenarios
 * - Glue code: Step definition classes in this package
 * - Plugin: Pretty console output and JSON report generation
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("scenarios")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.eflo.document.scenarios")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, json:target/cucumber-reports/cucumber.json, html:target/cucumber-reports/cucumber.html")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
@ConfigurationParameter(key = EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class CucumberTestRunner {
    // This class will be used by JUnit Platform to discover and run Cucumber tests
}
