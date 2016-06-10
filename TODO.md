TODO's
===================

- Does ResultsPanel get NoClassDefFoundError exception when calling
  ResultsPanelFactory.isReady()?
    - RankingPanel does this! bug or uncaught exception?
    * CONCLUSION: exception comes when running 'mvn clean install' -> not
      related to using the app in a normal way - no further actions taken
