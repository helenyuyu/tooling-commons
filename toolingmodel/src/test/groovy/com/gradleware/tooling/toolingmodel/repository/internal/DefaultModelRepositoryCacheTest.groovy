package com.gradleware.tooling.toolingmodel.repository.internal

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.DomainToolingClientSpecification
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.junit.Rule

class DefaultModelRepositoryCacheTest extends DomainToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  FixedRequestAttributes fixedRequestAttributes
  TransientRequestAttributes transientRequestAttributes
  DefaultModelRepository repository

  def setup() {
    // Gradle projects for testing
    directoryProvider.createFile('settings.gradle')
    directoryProvider.createFile('build.gradle') << 'task myTask {}'

    // request attributes and model repository for testing
    fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())
  }

  def "fetchBuildEnvironmentAndWait"() {
    when:
    def lookUp = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.gradle.gradleVersion == fourthLookUp.gradle.gradleVersion
    thirdLookUp.java.javaHome == fourthLookUp.java.javaHome
    thirdLookUp.java.jvmArguments == fourthLookUp.java.jvmArguments
  }

  def "fetchGradleBuildStructureAndWait"() {
    when:
    def lookUp = repository.fetchGradleBuildStructureAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchGradleBuildStructureAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchGradleBuildStructureAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchGradleBuildStructureAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchGradleBuildStructureAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.rootProject.path == fourthLookUp.rootProject.path
    thirdLookUp.rootProject.all.size() == fourthLookUp.rootProject.all.size()
  }

  def "fetchGradleBuildAndWait"() {
    when:
    def lookUp = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.rootProject.path == fourthLookUp.rootProject.path
    thirdLookUp.rootProject.all.size() == fourthLookUp.rootProject.all.size()
  }

  def "fetchEclipseGradleBuildAndWait"() {
    when:
    def lookUp = repository.fetchEclipseGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchEclipseGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchEclipseGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchEclipseGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchEclipseGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.rootProject.path == fourthLookUp.rootProject.path
    thirdLookUp.rootProject.all.size() == fourthLookUp.rootProject.all.size()
  }

//  def "fetchBuildInvocationsAndWait"() {
//    when:
//    def lookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)
//
//    then:
//    lookUp == null
//
//    when:
//    def firstLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def secondLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    assert firstLookUp != null
//    assert firstLookUp.is(secondLookUp)
//
//    when:
//    def thirdLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//    def fourthLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    then:
//    thirdLookUp != null
//    !thirdLookUp.is(fourthLookUp)
//    thirdLookUp.asMap()[':'].tasks.size() == fourthLookUp.asMap()[':'].tasks.size()
//    thirdLookUp.asMap()[':'].taskSelectors.size() == fourthLookUp.asMap()[':'].taskSelectors.size()
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_FromCacheOnly_CacheNotPopulated"() {
//    when:
//    def lookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)
//
//    then:
//    lookUp != null
//    lookUp.first == null
//    lookUp.second == null
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_FromCacheOnly_CachePopulated"() {
//    when:
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)
//
//    then:
//    compositeLookup != null
//    compositeLookup.first.is(gradleProjectLookUp)
//    compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_LoadIfNotCached_CacheNotPopulated"() {
//    when:
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    compositeLookup != null
//    compositeLookup.first.is(gradleProjectLookUp)
//    compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_LoadIfNotCached_CachePopulated"() {
//    when:
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    compositeLookup != null
//    compositeLookup.first.is(gradleProjectLookUp)
//    compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_LoadIfNotCached_RepeatedRead"() {
//    when:
//    def firstLookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def secondLookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    firstLookUp != null
//    !firstLookUp.is(secondLookUp)
//    firstLookUp.first.is(secondLookUp.first)
//    firstLookUp.second.is(secondLookUp.second)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_ForceReload_CacheNotPopulated"() {
//    when:
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    compositeLookup != null
//    compositeLookup.first.is(gradleProjectLookUp)
//    compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_ForceReload_CachePopulated"() {
//    when:
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    then:
//    compositeLookup != null
//    !compositeLookup.first.is(gradleProjectLookUp)
//    !compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_ForceReload_RepeatedRead"() {
//    when:
//    def firstLookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//    def secondLookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    then:
//    firstLookUp != null
//    !firstLookUp.is(secondLookUp)
//    !firstLookUp.first.is(secondLookUp.first)
//    !firstLookUp.second.is(secondLookUp.second)
//  }

}
