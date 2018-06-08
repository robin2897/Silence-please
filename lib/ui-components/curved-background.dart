import 'package:flutter/material.dart';

class CurvedBackground extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    bool isLandScape =
        MediaQuery.of(context).orientation == Orientation.landscape;
    return new FractionallySizedBox(
      heightFactor: isLandScape ? 0.8 : 0.6,
      widthFactor: 1.0,
      alignment: Alignment.topCenter,
      child: new ClipPath(
        clipper: new BottomWaveClipper(),
        child: new Container(
            decoration: new BoxDecoration(
          boxShadow: [
            new BoxShadow(
                offset: new Offset(0.0, 5.0),
                color: Colors.black12,
                blurRadius: 20.0,
                spreadRadius: 10.0),
          ],
          color: Theme.of(context).primaryColor,
        )),
      ),
    );
  }
}

class BottomWaveClipper extends CustomClipper<Path> {
  @override
  Path getClip(Size size) {
    Path path = new Path();
    path.lineTo(0.0, size.height - (size.height / 5));

    var controlPoints =
        new Offset(size.width / 2, size.height - (size.height / 12));
    var endPoints = new Offset(size.width, size.height - (size.height / 5));
    path.quadraticBezierTo(
        controlPoints.dx, controlPoints.dy, endPoints.dx, endPoints.dy);

    path.lineTo(size.width, size.height - (size.height / 5));
    path.lineTo(size.width, 0.0);

    path.close();
    return path;
  }

  @override
  bool shouldReclip(CustomClipper<Path> oldClipper) => false;
}
