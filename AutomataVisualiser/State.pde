import java.util.stream.Collectors;
import java.io.*;

class State implements Serializable {
  float x, y;
  boolean accepting;
  Map<Character, State> transitions;
  List<State> incomingTransitions;
  color colour;
  List<State> group;

  float diameter = 50;
  boolean isCurrent = false, hidden = false;

  State(float x, float y, boolean accepting, Map<Character, State> transitions, color colour) {
    this.x = x;
    this.y = y;
    this.accepting = accepting;
    this.transitions = transitions;

    incomingTransitions = new ArrayList();
    this.colour = colour;
  }

  State(color colour) {
    this(0, 0, false, new HashMap(), colour);
  }
  
  State() {
    this(color(255, 0, 0));
  }

  void show() {
    if (!hidden) {
      if (isCurrent) {
        strokeWeight(4);
      } else {
        strokeWeight(1);
      }
      fill(colour);
      circle(x, y, diameter);
      if (accepting) circle(x, y, 0.8*diameter);
    } else {
      noFill();
      circle(x, y, diameter);
    }
    strokeWeight(1);
  }

  void showTransitions() {
    if (hidden) return;
    for (Map.Entry<Character, State> transition : transitions.entrySet()) {
      if (transition.getValue().hidden) continue;

      //Draw the line between the two states
      float x2 = transition.getValue().x;
      float y2 = transition.getValue().y;
      line(x, y, x2, y2);

      float mx = (x2 + x)/2;
      float my = (y2 + y)/2;

      float a = atan2(x2-x, y2-y);

      float x3 = mx - 10*sin(a - PI/4);
      float y3 = my - 10*cos(a - PI/4);

      float x4 = mx - 10*sin(a + PI/4);
      float y4 = my - 10*cos(a + PI/4);

      //Draw the arrow in the direction of the transition
      line(mx, my, x3, y3);
      line(mx, my, x4, y4);
    }

    Map<State, List<Character>> groups = transitions.keySet().stream().collect(Collectors.groupingBy(transitions::get));

    for (Map.Entry<State, List<Character>> group : groups.entrySet()) {
      if (group.getKey().hidden) continue;

      float x2 = group.getKey().x;
      float y2 = group.getKey().y;

      float mx = (x2 + x)/2;
      float my = (y2 + y)/2;

      float a = atan2(x2-x, y2-y);

      float x5 = mx - 15*sin(a - PI/2);
      float y5 = my - 15*cos(a - PI/2);

      if (y5 > my) {
        y5 = my - (y5-my);
        x5 = mx - (x5-mx);
      }

      //Draw the symbol of the transition
      fill(0);
      String result = group.getValue().stream().map(String::valueOf).collect(Collectors.joining(","));
      text(result, x5, y5);
    }
  }


  boolean clicked() {
    return dist(mouseX, mouseY, screenX(x, y), screenY(x, y)) <= (diameter/2) * scale;
  }
}
