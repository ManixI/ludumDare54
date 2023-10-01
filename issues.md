## Resolved Issues ##

- **[1]** Skeletal, executable game
- **[2]** Bouncing Ball
- **[3]** Basic HUD/Console that can be toggled on and off, and can display FPS & bounces
- **[4]** Keyboard controls for ball
- **[5]** Add loading screen to get rid of glitchyness at start
- **[6]** Add inputAdapter to game and ensure HUD is compatible
- **[7]** Add console command to control ball's location/velocity
- **[8]** Add explosion animation
- **[9]** Add ball tracking HUD item
- **[10]** Add sounds and music
 - [11] Add platforms with collision
 - [12] add player avatar
 - [13] add jump physics
 - [14] add rng platforms
 - [15] add scrolling camera
 - [16] add death plane
 - [17] add life counter
 - [18] add score tracker
 - [19] add 1up
 - [20] add points collectable
 - [21] add spike enemy
 - [25] add custom avatar sprite
 - [26] add scrolling background
 - [27] add vertical scrolling
 - [29] add multiple platform paths
 - [30] fix speed of player so constantly in motion
 - [31] swap platforms to ArrayList from linked list
 - [32] add game over screen
 - [33] associate powerups with platform to guarantee at reachable height
 - [34] procedurally generate platforms as player moves
 - **[35]** more interesting platform patterns
 - [36] fix warping up when colliding with side of platform, should stop movement instead
 - [38] variable jump height, hold space to reduce gravity
 - [44] define play area with camera instead of gdx.graphics
 - [47] revert issues 27 and 29
 - [48] add ceiling
 - [49] change from platforms to floor
 - [51] change platforms to fit aesthetic
 - [53] update enemy spawning
 - [54] add double jump
 - [55] platforms shouldn't be pass-though
 - [62] fix collision issue with player sprite

## Open Issues ##

 - [0] general game balance tweeks
 - [22] add missile enemy
 - [23] add sfx
 - [24] add music
 - [28] add speed pads
 - [37] better points sprite
 - [39] increase speed over time
 - [40] zoom out as speed increases
 - [43] increase sense of speed
 - [50] add falling enemy
 - [52] add pits to floor
 - [56] add spawn groups to spawn pattern of objects instead of just within a random range
 - [57] freeze player animation on jump
 - [58] spinning sprite on second jump
 - [59] add spikes to bottom of platforms
 - [60] scale avatar size
 - [61] running sfx that scales with speed
 - [63] improve respawn mechanic

 ## Old ##
 - [41] add graphic for death plane
 - [42] different platform paths lean towards different directions
 - [45] spawn enemies as function of spawning platform
 - [46] set minimum height separation between platforms


## general ideas ##
In keeping with idea of limited space, have ceiling and floor. floor is RNG pits and floor
Ceiling has enemy spawns
floor has enemy and powerup spawns
player has double jump, but max jump easily hits ceiling
throwable hat
hat is one use
can hold up to 3 (stack on head)
collapsing ceiling that needs to be reset
needs to feel claustrophobic