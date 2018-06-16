import 'package:meta/meta.dart';

class SearchContactModel {
  final String name;
  final String phone;

  SearchContactModel({@required this.name, @required this.phone});

  factory SearchContactModel.fromJson(Map<String, dynamic> json) =>
      SearchContactModel(name: json['name'], phone: json['phone']);
}

class WhiteListModel {
  final String uuid;
  final String phone;

  WhiteListModel({@required this.uuid, @required this.phone});
}
