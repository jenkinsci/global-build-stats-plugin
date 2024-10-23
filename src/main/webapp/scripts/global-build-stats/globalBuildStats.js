Behaviour.specify(".gbs-check", "gbs-check", 0, function(element) {
  const checks = element.dataset.checks.split(",");
  checks.forEach(check => {
    if (check === "blur") {
      element.onblur = function() {
        validateField(element);
      }
    }
    if (check === "change") {
      element.onchange = function() {
        validateField(element);
      }
    }
  });
});

Behaviour.specify(".gbs-check-historicScale", "gbs-check-historicScale", 0, function(element) {
  element.onchange = function() {
    validateField(element);
    CURRENT_FORM.changeChartLengthUnit(element.value)
  }
});

Behaviour.specify(".gbs-initializeStats", "gbs-initializeStats", 0, function(element) {
  element.addEventListener("click", (e) => {
    ajaxCall('link', 'recordBuildInfos', function(transport) {
      document.getElementById('initializeStatsMessage').style.display = "inline";
    });
  });
});

Behaviour.specify(".gbs-refresh-stats", "gbs-refresh-stats", 0, function(element) {
  element.addEventListener("click", (e) => {
    e.preventDefault();
    window.location.href = ''
  });
});

Behaviour.specify(".gbs-edit-chart", "gbs-edit-chart", 0, function(element) {
  element.onclick = function() {
    const chartId = element.closest(".gbs-chart-buttons").dataset.id
    new BuildStatConfigForm(chartId).displayBuildStatConfigForm()
  }
});

Behaviour.specify(".gbs-chart-up", "gbs-chart-up", 0, function(element) {
  element.onclick = function() {
    const chartId = element.closest(".gbs-chart-buttons").dataset.id
    BUILD_STAT_CONFIGS.moveBuildStat(chartId, 'up')
  }
});

Behaviour.specify(".gbs-chart-down", "gbs-chart-down", 0, function(element) {
  element.onclick = function() {
    const chartId = element.closest(".gbs-chart-buttons").dataset.id
    BUILD_STAT_CONFIGS.moveBuildStat(chartId, 'down')
  }
});

Behaviour.specify(".gbs-delete-chart", "gbs-delete-chart", 0, function(element) {
  element.onclick = function() {
    const chartId = element.closest(".gbs-chart-buttons").dataset.id
    deleteBuildStat(chartId)
  }
});

Behaviour.specify(".gbs-days", "gbs-days", 0, function(element) {
  const id = element.dataset.id;
  const checkbox = document.getElementById(id);
  checkbox.onchange = function() {
    element.disabled = !checkbox.checked
  }
  checkbox.onchange();
});



BUILD_STAT_CONTAINER_ID_PREFIX = "container_buildStatConfig_";
var CURRENT_FORM = null;

var BUILD_STAT_CONFIGS = new BuildStatConfigs();
window.addEventListener('load', function(){
  ajaxCall('link', rootURL+'/plugin/global-build-stats/api/json?depth=2', function(ret) {
    var buildStat = JSON.parse(ret.responseText);
    for(i=0; i<buildStat.buildStatConfigs.length; i++){
      var config = buildStat.buildStatConfigs[i];
      BUILD_STAT_CONFIGS.add(config);
    }
  });
});

const constantsJson = document.getElementById("gbs-constants").text;
const constants = JSON.parse(constantsJson);
CHART_LENGTH_UNITS = constants.CHART_LENGTH_UNITS;
FIELD_FILTER_ALL = constants.FIELD_FILTER_ALL;
FIELD_FILTER_REGEX = constants.FIELD_FILTER_REGEX;
LAUNCHER_SYSTEM_REGEX = constants.LAUNCHER_SYSTEM_REGEX;
NODE_MASTER_REGEX = constants.NODE_MASTER_REGEX

