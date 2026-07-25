function verifyDates(){
  document.getElementById("datesError").innerHTML = ((document.getElementById("timeStart").value > document.getElementById("timeEnd").value)?generateErrorMessage(document.getElementById('swapDatesErrorMessage').innerHTML):"");
}
  
Behaviour.specify("#startTimeDisplay, #endTimeDisplay", "gsb-date-inputs", 0, function(fp) {
  flatpickr(fp, {
    altInput: true,
    altFormat: "Y-m-d H:i",
    allowInput: true,
    enableTime: true,
    wrap: true,
    clickOpens: false,
    dateFormat: "u",
    static: true,
    time_24hr: true,
    onChange: verifyDates,
  });
});

const constantsJson = document.getElementById("gbs-constants").text;
const constants = JSON.parse(constantsJson);
FIELD_FILTER_ALL = constants.FIELD_FILTER_ALL;
FIELD_FILTER_REGEX = constants.FIELD_FILTER_REGEX;
LAUNCHER_SYSTEM_REGEX = constants.LAUNCHER_SYSTEM_REGEX;
NODE_MASTER_REGEX = constants.NODE_MASTER_REGEX;
ESCAPED_JOB_FILTER = constants.ESCAPED_JOB_FILTER
ESCAPED_NODE_FILTER = constants.ESCAPED_NODE_FILTER
ESCAPED_LAUNCHER_FILTER = constants.ESCAPED_LAUNCHER_FILTER

window.addEventListener("load", function() {
  if(document.getElementById('searchBuild_jobFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
    document.getElementById('searchBuild_jobFilteringType_REGEX').checked = 'checked';
    initializeRegexField('searchBuild_jobNameRegex', ESCAPED_JOB_FILTER);
    document.getElementById('searchBuild_jobFilteringType_REGEX').onchange();
    document.getElementById('searchBuild_jobNameRegex').disabled = false;
  } else if(document.getElementById('searchBuild_jobFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
    document.getElementById('searchBuild_jobFilteringType_ALL').checked = 'checked';
    document.getElementById('searchBuild_jobFilteringType_ALL').onchange();
  }
  if(document.getElementById('searchBuild_nodeFilter').value.indexOf(NODE_MASTER_REGEX) != -1){
    document.getElementById('searchBuild_nodeFilteringType_REGEXMASTER').checked = 'checked';
    document.getElementById('searchBuild_nodeFilteringType_REGEXMASTER').onchange();
    document.getElementById('searchBuild_nodeNameRegex').disabled = true;
  } else if(document.getElementById('searchBuild_nodeFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
    document.getElementById('searchBuild_nodeFilteringType_REGEX').checked = 'checked';
    initializeRegexField('searchBuild_nodeNameRegex', ESCAPED_NODE_FILTER);
    document.getElementById('searchBuild_nodeFilteringType_REGEX').onchange();
    document.getElementById('searchBuild_nodeNameRegex').disabled = false;
  } else if(document.getElementById('searchBuild_nodeFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
    document.getElementById('searchBuild_nodeFilteringType_ALL').checked = 'checked';
    document.getElementById('searchBuild_nodeFilteringType_ALL').onchange();
  }
  if(document.getElementById('searchBuild_launcherFilter').value.indexOf(LAUNCHER_SYSTEM_REGEX) != -1){
    document.getElementById('searchBuild_launcherFilteringType_REGEXSYSTEM').checked = 'checked';
    document.getElementById('searchBuild_launcherFilteringType_REGEXSYSTEM').onchange();
    document.getElementById('searchBuild_launcherNameRegex').disabled = true;
  } else if(document.getElementById('searchBuild_launcherFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
    document.getElementById('searchBuild_launcherFilteringType_REGEX').checked = 'checked';
    initializeRegexField('searchBuild_launcherNameRegex', ESCAPED_LAUNCHER_FILTER);
    document.getElementById('searchBuild_launcherFilteringType_REGEX').onchange();
    document.getElementById('searchBuild_launcherNameRegex').disabled = false;
  } else if(document.getElementById('searchBuild_launcherFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
    document.getElementById('searchBuild_launcherFilteringType_ALL').checked = 'checked';
    document.getElementById('searchBuild_launcherFilteringType_ALL').onchange();
  }
});
