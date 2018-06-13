import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:meta/meta.dart';

import '../model/appmodel.dart';
import '../redux/state-manager.dart';
import '../redux/view-model.dart';
import '../util/sperator.dart';
import '../util/validator.dart';

class FloatingAction extends StatelessWidget {
  final GlobalKey<ScaffoldState> scaffoldKey;

  FloatingAction({@required this.scaffoldKey});

  @override
  Widget build(BuildContext context) {
    return new FloatingActionButton.extended(
      label: new Text("Add",
          style: Theme.of(context).textTheme.button.copyWith(fontSize: 15.0)),
      icon: new Icon(Icons.add),
      onPressed: () {
        showAddSheet(context: context);
      },
    );
  }

  void showAddSheet({BuildContext context}) async {
    showModalBottomSheet(
        context: context,
        builder: (cxt) {
          return new _AddBottomSheet(scaffoldKey: scaffoldKey);
        });
  }
}

class _AddBottomSheet extends StatefulWidget {
  final GlobalKey<ScaffoldState> scaffoldKey;

  _AddBottomSheet({@required this.scaffoldKey});

  @override
  _AddBottomSheetState createState() => new _AddBottomSheetState();
}

class _AddBottomSheetState extends State<_AddBottomSheet> {
  bool isLandscape;
  TimeOfDay now = new TimeOfDay.now();
  TimeOfDay startWhen = new TimeOfDay.now();
  TimeOfDay endWhen = new TimeOfDay.fromDateTime(
      new DateTime.now().add(const Duration(hours: 1)));

  @override
  Widget build(BuildContext context) {
    isLandscape = MediaQuery.of(context).orientation == Orientation.landscape;
    TimeExtractor startWhenExac =
        timeExtractor(rawTime: startWhen, context: context);
    TimeExtractor endWhenExac =
        timeExtractor(rawTime: endWhen, context: context);

    return new Scaffold(
        floatingActionButton: new StoreConnector<AppState, ViewModel>(
          converter: (store) => new ViewModel.add(
              addAction: (model) => store.dispatch(AddAction(
                    model: model,
                  ))),
          builder: (cxt, viewModel) {
            return FloatingActionButton.extended(
              icon: new Icon(Icons.done),
              label: new Text("Done",
                  style: Theme
                      .of(context)
                      .textTheme
                      .button
                      .copyWith(fontSize: 15.0)),
              onPressed: () {
                var model = new AppModel.fromTimeOfDay(
                    startTime: startWhen,
                    endTime: endWhen,
                    days: [1, 1, 1, 1, 1, 1, 1],
                    isActive: true,
                    isSilent: true,
                    isVibrate: false);
                performAdd(model, viewModel);
                Navigator.pop(context);
              },
            );
          },
        ),
        body: new Row(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            isLandscape
                ? new Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: <Widget>[
                      startEndTimeSelector(
                          label: "Start when",
                          time: startWhenExac.time,
                          suffix: startWhenExac.suffix,
                          ontap: () => startWhenOnTap(time: startWhen)),
                      new Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 15.0),
                      ),
                      startEndTimeSelector(
                          label: "End when",
                          time: endWhenExac.time,
                          suffix: endWhenExac.suffix,
                          ontap: () => endWhenOnTap(time: endWhen)),
                    ],
                  )
                : new Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: <Widget>[
                      startEndTimeSelector(
                          label: "Start when",
                          time: startWhenExac.time,
                          suffix: startWhenExac.suffix,
                          maxWidth: true,
                          ontap: () => startWhenOnTap(time: startWhen)),
                      new Padding(
                        padding: const EdgeInsets.symmetric(vertical: 15.0),
                      ),
                      startEndTimeSelector(
                          label: "End when",
                          time: endWhenExac.time,
                          suffix: endWhenExac.suffix,
                          maxWidth: true,
                          ontap: () => endWhenOnTap(time: endWhen)),
                    ],
                  )
          ],
        ));
  }

  Widget startEndTimeSelector(
      {String label,
      String time,
      String suffix,
      Function ontap,
      bool maxWidth: false}) {
    return new Container(
      width: !maxWidth ? null : MediaQuery.of(context).size.width,
      child: new InkWell(
        onTap: ontap,
        child: new Center(
          child: new Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              new Text(
                label,
                style:
                    Theme.of(context).textTheme.body1.copyWith(fontSize: 20.0),
              ),
              new Row(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  new Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    crossAxisAlignment: CrossAxisAlignment.baseline,
                    textBaseline: TextBaseline.alphabetic,
                    children: <Widget>[
                      new Padding(
                        padding: const EdgeInsets.only(right: 10.0),
                        child: new Center(
                            child: new Icon(
                          Icons.access_time,
                          size: 50.0,
                        )),
                      ),
                      new Text(
                        time,
                        style: Theme
                            .of(context)
                            .textTheme
                            .body1
                            .copyWith(fontSize: 70.0),
                      ),
                      new Text(
                        suffix,
                        style: Theme
                            .of(context)
                            .textTheme
                            .body1
                            .copyWith(fontSize: 20.0),
                      ),
                    ],
                  ),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }

  Future<TimeOfDay> showTimeSelector({@required TimeOfDay time}) async {
    return await showTimePicker(context: context, initialTime: time);
  }

  void startWhenOnTap({@required TimeOfDay time}) async {
    startWhen = await showTimeSelector(time: time) ?? startWhen;
    setState(() {});
  }

  void endWhenOnTap({@required TimeOfDay time}) async {
    endWhen = await showTimeSelector(time: time) ?? endWhen;
    setState(() {});
  }

  String get message {
    int hrs = startWhen.hour - now.hour;
    int mins = startWhen.minute - now.minute;
    if (hrs <= 0 && mins < 0) {
      hrs = 24 + hrs;
      mins = 60 + mins;
    } else if (hrs == 0 && mins == 0) {
      return "Silence is set for a day now";
    }
    var hours = hrs == 0 ? "" : '$hrs hrs';
    return "Silence is set for $hours and $mins mins from now";
  }

  void performAdd(AppModel model, ViewModel viewModel) async {
    if (Validator().checkIsEndTimeGreatorThanStartTime(model)) {
      var value = await Validator().checkTimesIsNotConflictingWithOther(model);
      if (value) {
        viewModel.addAction(model);
        widget.scaffoldKey.currentState.showSnackBar(new SnackBar(
          content: new Text(
            message,
            style: Theme
                .of(context)
                .textTheme
                .body1
                .copyWith(fontSize: 16.0),
          ),
        ));
      } else {
        widget.scaffoldKey.currentState.showSnackBar(new SnackBar(
          content: new Text(
            "Time is conflicting with other",
            style: Theme
                .of(context)
                .textTheme
                .body1
                .copyWith(fontSize: 16.0),
          ),
        ));
      }
    } else {
      widget.scaffoldKey.currentState.showSnackBar(new SnackBar(
        content: new Text(
          "End time is must be greater then Start time",
          style: Theme
              .of(context)
              .textTheme
              .body1
              .copyWith(fontSize: 16.0),
        ),
      ));
    }
    widget.scaffoldKey.currentState.setState(() {});
  }
}
