import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class LicenseScreen extends StatelessWidget {
  final List<LicenseModel> listOfLicense = [
    new LicenseModel(
        "flutter", "https://github.com/flutter/flutter/blob/master/LICENSE"),
    new LicenseModel("path_provider",
        "https://github.com/flutter/plugins/blob/master/packages/path_provider/LICENSE"),
    new LicenseModel(
        "redux", "https://github.com/johnpryan/redux.dart/blob/master/LICENSE"),
    new LicenseModel("flutter_redux",
        "https://github.com/brianegan/flutter_redux/blob/master/LICENSE"),
    new LicenseModel("shared_preferences",
        "https://github.com/flutter/plugins/blob/master/packages/shared_preferences/LICENSE"),
    new LicenseModel("json_annotation",
        "https://github.com/dart-lang/json_serializable/blob/master/LICENSE"),
    new LicenseModel("build_runner",
        "https://github.com/dart-lang/build/blob/master/build_runner/LICENSE"),
    new LicenseModel("json_serializable",
        "https://github.com/dart-lang/json_serializable/blob/master/LICENSE"),
    new LicenseModel("url_launcher",
        "https://github.com/flutter/plugins/blob/master/packages/url_launcher/LICENSE"),
    new LicenseModel("Android Room",
        "https://developer.android.com/topic/libraries/architecture/room"),
    new LicenseModel("android-job",
        "https://github.com/evernote/android-job/blob/master/LICENSE"),
    new LicenseModel("libphonenumber",
        "https://github.com/googlei18n/libphonenumber/blob/master/LICENSE"),
  ];

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      backgroundColor: Colors.white,
      appBar: new AppBar(title: Text("Licenses")),
      body: new Theme(
        data: Theme.of(context).copyWith(brightness: Brightness.light),
        child: new ListView.builder(
          itemBuilder: (cxt, currIndex) {
            String url = listOfLicense[currIndex].url;
            String title = listOfLicense[currIndex].title;
            return new ListTile(
              title: new Text(title),
              trailing: new IconButton(
                icon: Icon(Icons.link),
                onPressed: () => _launchUrl(url),
              ),
            );
          },
          itemCount: listOfLicense.length,
        ),
      ),
    );
  }

  _launchUrl(String goto) async {
    if (await canLaunch(goto)) {
      await launch(goto);
    } else {
      throw 'Could not launch $goto';
    }
  }
}

class LicenseModel {
  final String title;
  final String url;
  LicenseModel(this.title, this.url);
}
