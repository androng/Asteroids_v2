# Asteroids_v2
A spin from the Atari Asteroids game from 1979, but with more weapons. I didn't program in death so anyone playing has to use cheat codes to add more enemies. 

This is a rewrite of my original assignment from Mr. Simon's AP Computer Science class. https://github.com/APCSLowell/AsteroidsGame



## Features 
* Decked out ship with laser, repellant, chain lightning and zero laser.
* Options panel for changing the color of your ship 
* Options panel for changing controls (doesn't work on Mac)
* Anti-aliasing for smoother rendering

## Controls
Move - Left Mouse button OR WASD OR Arrow Keys

Space - Fire primary weapon

Tab - Show weapon cooldowns 

Left CTRL - Hyperspace 

1-9 - Weapons

0 - Toggle Debug mode (adds a shield, unlimited bullets and gives a lot more HP) 

F1 - Options Panel for changing ship color and game controls

F5 - Partially lower health 

F6 - Multiply speed by 1.1

F8 - Kill all asteroids 

F9 - Add new asteroids

F11 - Add new small asteroids

F12 - Restart game

## Install
All code is in the src folder.
Asteroids can either be a Java Applet or Java Application. `main()` is in `mainClass.java`.

## Motivation 
This was my form of artwork in high school: developing a bunch of weapons to kill imaginary asteroids using nothing but lines and circles as building blocks. It was like being a space engineer without any money, any physics or electrical or mechanical knowledge. 

### Why didn't you make it into a commercial game? 
I thought hard about making this into a real game that I could sell on Steam, but I felt like that was 10x the work. I would have had to do the level design, stats balancing, boss design, sound design, customer feedback, optimization to 60+ FPS, visual polish beyond lines and circles. A new "Geometry wars" had just been released too, so my motivation died because I didn't want to release an inferior copy. Yes I can carve out another niche within the arcade genre. I just have to be more creative.

### How much time did you spend on this? 
A few hundred hours. 

### Which weapon was the hardest to make
The reinforcements and chain lighting were very difficult. I had to think of all the psudocode in my head before writing anything. You can read it in `Reinforcements outline.txt`

## I want some ideas to make my Asteroids unique
Weapon ideas: black hole, lasso, powerups, snakes, fire, gravity wave

Mechanics ideas: play the map on a 3D sphere instead of a 2D rectangle, control more than one ship at a time, make a space tower defense

Alternatively, remove the space theme and make another kind of shooter

## License 
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.