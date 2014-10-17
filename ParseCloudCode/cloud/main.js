Parse.Cloud.define("push", function(request, response) {
    var query = new Parse.Query(Parse.Installation);
    query.equalTo("fbid", request.params.fbid);
    var fbname = request.params.fbname;
    var placename = request.params.placename;
    var vicinity = request.params.vicinity;

    Parse.Push.send({
        where: query,
        data : {
            alert: fbname + " has invited you to eat at " + placename + " (" + vicinity + ")!"
        }
    }, {
        success: function() {
            response.success("Sent notification to " + fbName);
        },
        error: function(error) {
            response.error("Notification not sent");
        }
    });
});
