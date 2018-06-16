import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';

import '../util/plugin.dart';
import '../model/contactmodel.dart';

class WhiteList extends StatefulWidget {
  @override
  _WhiteListState createState() => _WhiteListState();
}

class _WhiteListState extends State<WhiteList> {
  final scaffoldKey = new GlobalKey<ScaffoldState>();

  final searchController = new TextEditingController();
  bool isSearching = false;
  List<SearchContactModel> searchList = [];
  List<WhiteListModel> whiteListedContactList = [];
  EventChannel eventChannel = PluginHandShake.contactEventChannel;

  void handleSearchBegin() {
    ModalRoute
        .of(context)
        .addLocalHistoryEntry(new LocalHistoryEntry(onRemove: () {
      setState(() {
        isSearching = false;
        searchController.clear();
        searchList.clear();
      });
    }));
    setState(() {
      isSearching = true;
    });
  }

  void searchContact(String query) {
    PluginHandShake().searchContact(query);
  }

  void updateSearchList(Object event) {
    if (event != "no-data") {
      setState(() {
        var list = event as List<dynamic>;
        searchList = list
            .map((f) => new SearchContactModel.fromJson(json.decode(f)))
            .toList();
      });
    }
  }

  void whiteListInsert(String phone) async {
    for (var item in whiteListedContactList) {
      if (item.phone == phone) {
        scaffoldKey.currentState.showSnackBar(new SnackBar(
          content: new Text("Already present",
              style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16.0)),
        ));
        return;
      }
    }
    String uuid = Uuid().v1().toString();
    String result = await PluginHandShake()
        .whiteListOp(PluginHandShake.WHITE_LIST_OP_INSERT, phone, uuid);
    if (result == "success") {
      setState(() {
        whiteListedContactList
            .add(new WhiteListModel(uuid: uuid, phone: phone));
        scaffoldKey.currentState.showSnackBar(new SnackBar(
          content: new Text("Added",
              style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16.0)),
        ));
      });
    } else {
      scaffoldKey.currentState.showSnackBar(new SnackBar(
        content: new Text(result,
            style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16.0)),
      ));
    }
  }

  void whiteListDelete(WhiteListModel model, int index) async {
    String result = await PluginHandShake()
        .whiteListOp(PluginHandShake.WHITE_LIST_OP_DELETE, model.uuid);
    if (result == "success") {
      setState(() {
        whiteListedContactList.removeAt(index);
      });
    }
  }

  void whiteListAll() async {
    Map<dynamic, dynamic> result =
        await PluginHandShake().whiteListOp(PluginHandShake.WHITE_LIST_OP_ALL);
    print(result);
    if (result.isNotEmpty) {
      List<WhiteListModel> temp = [];
      result.forEach((k, v) {
        temp.add(new WhiteListModel(uuid: k, phone: v));
      });
      setState(() {
        whiteListedContactList.addAll(temp);
      });
    }
  }

  Widget buildSearchAppbar() {
    return new AppBar(
      leading: new BackButton(
        color: Colors.white,
      ),
      title: new TextField(
        controller: searchController,
        autofocus: true,
        onChanged: (v) => searchContact(v),
        decoration: const InputDecoration(
          hintText: 'Add Phone Number',
        ),
        style: new TextStyle(color: Colors.white, fontSize: 18.0),
        onSubmitted: (value) => whiteListInsert(value),
      ),
      actions: <Widget>[
        new IconButton(
          icon: Icon(Icons.done),
          onPressed: () => whiteListInsert(searchController.value.text),
        )
      ],
      backgroundColor: Theme.of(context).canvasColor,
    );
  }

  Widget buildAppbar() {
    return new AppBar(
      leading: new BackButton(
        color: Colors.white,
      ),
      title: new Text("White List"),
      actions: <Widget>[
        new IconButton(
          icon: const Icon(Icons.search),
          onPressed: handleSearchBegin,
          tooltip: 'Search',
        ),
      ],
      backgroundColor: Theme.of(context).canvasColor,
    );
  }

  Widget searchListItem(String name, String phone) {
    return new Padding(
      padding: const EdgeInsets.all(5.0),
      child: new Card(
        color: Colors.white,
        child:  new ListTile(
          title: new Text(
            name == null ? phone : name,
            style: new TextStyle(fontSize: 20.0),
          ),
          subtitle: new Text(phone, style: new TextStyle(fontSize: 18.0)),
          onTap: () => whiteListInsert(phone),
        ),
      ),
    );
  }

  Widget whiteListedContact(WhiteListModel model, int index) {
    return new Padding(
      padding: const EdgeInsets.all(5.0),
      child: new Card(
        color: Colors.white,
        child: new ListTile(
          title: new Text(model.phone, style: new TextStyle(fontSize: 20.0)),
          trailing: new IconButton(
            icon: new Icon(Icons.delete),
            onPressed: () => whiteListDelete(model, index),
            color: Colors.black54,
          ),
        ),
      ),
    );
  }

  @override
  void initState() {
    super.initState();
    eventChannel.receiveBroadcastStream().listen(updateSearchList);
    whiteListAll();
  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      key: scaffoldKey,
      appBar: isSearching ? buildSearchAppbar() : buildAppbar(),
      backgroundColor: Color(0xFFEEEEEE),
      floatingActionButton: new FloatingActionButton.extended(
        icon: new Icon(Icons.add),
        label: new Text("Add Phone"),
        onPressed: handleSearchBegin,
      ),
      body: new Theme(
        data: Theme.of(context).copyWith(brightness: Brightness.light),
        child: new NestedScrollView(
          headerSliverBuilder: (context, innerBoxIsScrolled) {
            return <Widget>[
              new SliverList(
                delegate: new SliverChildBuilderDelegate((context, curIndex) {
                  return searchListItem(
                      searchList[curIndex].name, searchList[curIndex].phone);
                }, childCount: searchList.length),
              )
            ];
          },
          body: new ListView.builder(
            padding: const EdgeInsets.symmetric(vertical: 6.0),
            itemBuilder: (context, curIndex) {
              return whiteListedContact(
                  whiteListedContactList[curIndex], curIndex);
            },
            itemCount: whiteListedContactList.length,
          ),
        ),
      ),
    );
  }
}
