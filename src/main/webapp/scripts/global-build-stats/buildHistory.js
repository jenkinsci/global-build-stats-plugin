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
    dateFormat: "U",
    static: true,
    time_24hr: true,
    onChange: verifyDates,
  });
});