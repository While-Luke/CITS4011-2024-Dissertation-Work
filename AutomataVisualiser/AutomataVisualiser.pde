/**
 Mouse Controls:
 Left click: Select state
 Left click + control: Select multiple states
 Left click + drag: Move states around
 Right click: Deselect all states
 Right click + drag: Move camera around
 Scroll wheel: Zoom in or out
 Middle click: Hide/unhide selected state
 
 Keybinds:
 h: hide/unhide all non-strongly connected states
 g: select all states in currently selected strongly connected group
 a: arrange all currently selected states to near the mouse
 q/e: rotate all currently selected states anticlockwise/clockwise
 f/v: flip all currently selected states horiztonally/vertically
 c: move all the camera to the center of the states
 +/-: Increase/decrease transitions text size
 s: save the current states and their positions to a file
 l: load states from file
 **/


import java.util.*;

float camx, camy;
float scale = 1;
int textSize = 12;

boolean mouseL = false, mouseR = false;
boolean control = false;

List<State> states;
State startState;
List<State> current;

void setup() {
  fullScreen(P2D);

  ellipseMode(CENTER);

  camx = 0;
  camy = 0;

  states = readFile("automata.txt");

  current = new ArrayList();
}

void draw() {
  background(255);

  textSize(textSize);

  scale(scale);
  translate(camx, camy);

  if (mouseR) {
    camx += (mouseX - pmouseX) / scale;
    camy += (mouseY - pmouseY) / scale;
  }
  if (mouseL && current != null) {
    for (State c : current) {
      c.x += (mouseX - pmouseX) / scale;
      c.y += (mouseY - pmouseY) / scale;
    }
  }

  for (State s : states) s.showTransitions();
  for (State s : states) s.show();
}


void mousePressed() {
  if (mouseButton == RIGHT) {
    mouseR = true;

    if (current.size() > 0) {
      for (State c : current) c.isCurrent = false;

      current.clear();
    }
  }

  if (mouseButton == LEFT) {
    mouseL = true;

    State clicked = null;
    for (State s : states) {
      if (s.clicked()) {
        clicked = s;
        break;
      }
    }
    if (clicked != null && !clicked.isCurrent) {
      if(!control && current.size() > 0){
        for (State c : current) c.isCurrent = false;

        current.clear();
      }
      
      current.add(clicked);
      clicked.isCurrent = true;
    }
  }

  if (mouseButton == CENTER) {
    for (State s : states) {
      if (s.clicked()) {
        s.hidden = !s.hidden;
      }
    }
  }
}

void mouseReleased() {
  if (mouseButton == RIGHT) mouseR = false;
  if (mouseButton == LEFT)  mouseL = false;
}

void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  scale -= e/20;
}

void keyPressed() {
  switch(key) {

    //Hide/unhide unimportant states
  case 'h':
    boolean alreadyHidden = true;
    for (State s : states) {
      if (s.colour == color(0) && !s.hidden) {
        alreadyHidden = false;
        break;
      }
    }
    if (alreadyHidden) {
      for (State s : states) {
        if (s.colour == color(0)) s.hidden = false;
      }
    } else {
      for (State s : states) {
        if (s.colour == color(0)) s.hidden = true;
      }
    }
    break;

    //Select all states in the same group
  case 'g':
    if (current.size() > 0) {
      current = new ArrayList(current.get(0).group);
      for (State c : current) {
        c.isCurrent = true;
      }
    }
    break;

    //Arrange all states in the same group around the mouse
  case 'a':
    if (current.size() == 1 && current.get(0).colour != color(0)) {
      current = new ArrayList(current.get(0).group);
      for (State c : current) {
        c.isCurrent = true;
      }
    }
    if (current.size() > 1) {
      int n = current.size();
      float alpha = TWO_PI/n;
      float beta =(n-2)*QUARTER_PI/n;
      float h = 200*sin(beta)/sin(alpha);
      for (int i = 0; i < current.size(); i++) {
        current.get(i).x = mouseX / scale - camx + (cos(2*PI*i/n) * h);
        current.get(i).y = mouseY / scale - camy + (sin(2*PI*i/n) * h);
      }
    }
    break;

    //Rotate all selected states anticlockwise
  case 'q':
    float centerx = 0;
    float centery = 0;
    for (State s : current) {
      centerx += s.x;
      centery += s.y;
    }
    centerx /= current.size();
    centery /= current.size();

    for (State s : current) {
      float a = atan2(s.y-centery, s.x-centerx);
      float d = dist(s.x, s.y, centerx, centery);

      s.x = centerx + d*cos(a-QUARTER_PI);
      s.y = centery + d*sin(a-QUARTER_PI);
    }
    break;

    //Rotate all selected states clockwise
  case 'e':
    centerx = 0;
    centery = 0;
    for (State s : current) {
      centerx += s.x;
      centery += s.y;
    }
    centerx /= current.size();
    centery /= current.size();

    for (State s : current) {
      float a = atan2(s.y-centery, s.x-centerx);
      float d = dist(s.x, s.y, centerx, centery);

      s.x = centerx + d*cos(a+QUARTER_PI);
      s.y = centery + d*sin(a+QUARTER_PI);
    }
    break;

    //Flip all selected states horizontally
  case 'f':
    centerx = 0;
    for (State s : current) {
      centerx += s.x;
    }
    centerx /= current.size();

    for (State s : current) {
      s.x = centerx - (s.x-centerx);
    }
    break;

    //Flip all selected states vertically
  case 'v':
    centery = 0;
    for (State s : current) {
      centery += s.y;
    }
    centery /= current.size();

    for (State s : current) {
      s.y = centery - (s.y-centery);
    }
    break;

    //Move the camera to the center of the states
  case 'c':
    float totalx = 0;
    float totaly = 0;
    for (State s : states) {
      totalx += s.x;
      totaly += s.y;
    }
    camx = totalx / states.size();
    camy = totaly / states.size();
    break;

    //Increase text size
  case '+':
  case '=':
    textSize+=2;
    break;

    //Descrease text size
  case '-':
    textSize = max(0, textSize-2);
    break;

    //Save the current states and their positions to a file
  case 's':
    PrintWriter writer = createWriter("save.txt");
    Map<State, String> map = new HashMap();

    for (int i = 0; i < states.size(); i++) map.put(states.get(i), str(i));

    for (State s : states) {
      writer.print(s.x + "," + s.y + "," + s.accepting + "," + s.colour + "," + s.hidden);
      for (Map.Entry<Character, State> entry : s.transitions.entrySet()) {
        writer.print("," + entry.getKey() + ":" + map.get(entry.getValue()));
      }
      writer.print(";");
      writer.print(s.incomingTransitions.stream().map(map::get).collect(Collectors.joining(",")));
      writer.print(";");
      if (s.group != null) {
        writer.print(s.group.stream().map(map::get).collect(Collectors.joining(",")));
      }
      writer.print("\n");
    }

    writer.flush();
    writer.close();

    break;

    //Load states from file
  case 'l':
    BufferedReader reader = createReader("save.txt");
    String line = null;
    int count = 0;
    Map<String, State> newStates = new HashMap();
    try {
      while ((line = reader.readLine()) != null) {

        String[] sections = split(line, ";");

        String[] part1 = split(sections[0], ",");

        float x = float(part1[0]);
        float y = float(part1[1]);
        boolean accepting = boolean(part1[2]);
        color colour = color(int(part1[3]));
        boolean hidden = boolean(part1[4]);

        Map<Character, State> transitions = new HashMap();
        for (int i = 5; i < part1.length; i++) {
          Character tKey = split(part1[i], ":")[0].charAt(0);
          String tValue = split(part1[i], ":")[1];
          State s;
          if (newStates.containsKey(tValue)) {
            s = newStates.get(tValue);
          } else {
            s = new State();
            newStates.put(tValue, s);
          }
          transitions.put(tKey, s);
        }

        List<State> incomingTransitions = new ArrayList();
        if (!sections[1].equals("")) {
          for (String str : split(sections[1], ",")) {
            State s;
            if (newStates.containsKey(str)) {
              s = newStates.get(str);
            } else {
              s = new State();
              newStates.put(str, s);
            }
            incomingTransitions.add(s);
          }
        }

        List<State> group = new ArrayList();
        if (!sections[2].equals("")) {
          for (String str : split(sections[2], ",")) {
            State s;
            if (newStates.containsKey(str)) {
              s = newStates.get(str);
            } else {
              s = new State();
              newStates.put(str, s);
            }
            group.add(s);
          }
        }

        State s;
        String k = str(count);
        if (newStates.containsKey(k)) {
          s = newStates.get(k);
          s.x = x;
          s.y = y;
          s.accepting = accepting;
          s.colour = colour;
          s.hidden = hidden;
          s.transitions = transitions;
          s.incomingTransitions = incomingTransitions;
          s.group = group;
        } else {
          s = new State(x, y, accepting, transitions, colour);
          s.hidden = hidden;
          s.incomingTransitions = incomingTransitions;
          s.group = group;
          newStates.put(k, s);
        }
        count++;
      }
      states = new ArrayList(newStates.values());
      current.clear();
      reader.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    break;
    
    //Hide all non-accepting states
    case 'p':
    for(State s : states){
      if(s.accepting){
        s.x = mouseX / scale - camx;
        s.y = mouseY / scale - camy;
      }
      else{
        s.hidden = true;
      }
    }
    break;
  }
  
  if(keyCode == CONTROL) control = true;
}

void keyReleased(){
  if(keyCode == CONTROL) control = false;
}


List<State> readFile(String filename) {
  Map<String, State> automata = new HashMap();

  try {
    BufferedReader reader = createReader(filename);
    
    String line = null;
    try{
      line = reader.readLine();
    }
    catch(NullPointerException e){
      exit();
      return null;
    }

    if (!line.equals("#states")) throw new RuntimeException("Error while reading file - 0");

    //Read in the state names
    while ((line = reader.readLine()) != null) {
      if (line.equals("#initial")) break;

      automata.put(line.strip(), new State(color(0)));
    }

    //Read in the start state
    line = reader.readLine();
    startState = automata.get(line);

    //Read in the accepting states
    line = reader.readLine();
    if (!line.equals("#accepting")) throw new RuntimeException("Error while reading file - 1");

    while ((line = reader.readLine()) != null) {
      if (line.equals("#alphabet")) break;

      automata.get(line).accepting = true;
    }

    //Read in the alphabet
    while ((line = reader.readLine()) != null) {
      if (line.equals("#transitions")) break;
    }

    //Read in the transitions
    while ((line = reader.readLine()) != null) {
      String[] s = split(line, ':');
      String[] s2 = split(s[1], '>');

      String state1 = s[0];
      Character symbol = s2[0].charAt(0);
      String state2 = s2[1];

      automata.get(state1).transitions.put(symbol, automata.get(state2));
      automata.get(state2).incomingTransitions.add(automata.get(state1));
    }

    //Arange the states
    List<State> seen = new ArrayList();
    seen.add(startState);

    List<List<State>> order = new ArrayList();
    List<State> next = new ArrayList();
    next.add(startState);

    while (!next.isEmpty()) {
      order.add(next);
      next = new ArrayList();

      for (State s : order.get(order.size()-1)) {
        for (State n : s.transitions.values()) {
          if (!seen.contains(n)) {
            seen.add(n);
            next.add(n);
          }
        }
      }
    }

    for (int i = 0; i < order.size(); i++) {
      for (int j = 0; j < order.get(i).size(); j++) {
        order.get(i).get(j).x = i * 200 + random(100) - 50;
        order.get(i).get(j).y = j * 200 - (order.get(i).size() * 100);
      }
    }

    colourGroups(automata);

    reader.close();
  }
  catch (IOException e) {
    e.printStackTrace();
    exit();
    return null;
  }

  return new ArrayList(automata.values());
}

void colourGroups(Map<String, State> automata) {
  //https://en.wikipedia.org/wiki/Kosaraju%27s_algorithm

  List<State> L = new ArrayList();

  Map<State, Boolean> visited = new HashMap();
  for (State s : automata.values()) {
    visited.put(s, false);
  }

  for (State s : automata.values()) {
    visit(s, visited, L);
  }

  for (State s : automata.values()) {
    visited.put(s, false);
  }
  Map<State, List<State>> group = new HashMap();
  for (State s : L) {
    assign(s, s, group, visited);
  }

  group.keySet().removeIf(s -> group.get(s).size() < 2);

  long numGroups = group.values().size();

  colorMode(HSB, 360);
  int count = 0;
  for (List<State> l : group.values()) {
    for (State s : l) {
      float hue = map(count, 0, numGroups, 0, 360);
      s.colour = color(hue, 360, 360);
      s.group = l;
    }
    count++;
  }
  colorMode(RGB, 255, 255, 255);
}

void visit(State u, Map<State, Boolean> visited, List<State> L) {
  if (visited.get(u)) return;

  visited.put(u, true);
  for (State s : u.transitions.values()) visit(s, visited, L);
  L.add(0, u);
}

void assign(State u, State root, Map<State, List<State>> group, Map<State, Boolean> visited) {
  if (visited.get(u)) return;
  visited.put(u, true);

  group.putIfAbsent(root, new ArrayList());
  group.get(root).add(u);
  for (State s : u.incomingTransitions) assign(s, root, group, visited);
}
