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
 - **[22]** add missile enemy
 - **[23]** add sfx
 - **[24]** add music
 - [25] add custom avatar sprite
 - [26] add scrolling background
 - [27] add vertical scrolling
 - [28] add speed pads
 - [29] add multiple platform paths
 - [30] fix speed of player so constantly in motion
 - [31] swap platforms to ArrayList from linked list
 - [32] add game over screen
 - [33] associate powerups with platform to guarantee at reachable height
 - [34] procedurally generate platforms as player moves
 - **[35]** more interesting platform patterns
 - [36] fix warping up when colliding with side of platform, should stop movement instead
 - [38] variable jump height, hold space to reduce gravity
 - **[39]** increase speed over time
 - [44] define play area with camera instead of gdx.graphics
 - [47] revert issues 27 and 29
 - [48] add ceiling
 - [49] change from platforms to floor
 - [50] add falling/beam enemy
 - [51] change platforms to fit aesthetic
 - [53] update enemy spawning
 - [54] add double jump
 - [55] platforms shouldn't be pass-though
 - [57] freeze player animation on jump
 - [60] scale avatar size
 - [62] fix collision issue with player sprite
 - **[63]** improve respawn mechanic
 - **[64]** re-start button
 - **[65]** fix rounding error preventing enemies from spawning
 - [66] add button sfx
 - [67] fix platforms spawning below screen
 - [68] remove ceiling and floor
 - [69] multiple platform paths
 - [70] vertical scrolling
 - [72] only check collision for things on screen
 - [73] fix bug where score increases after death
 - [75] increase player size
 - **[78]** add background
 - [81] bounding rectangle draw function
 - [82] change how camera operates
 - [86] jump up though platforms
 - [87] better player re-spawn position
 - [88] better game over screen
 - **[89]** fix game hud crashing game when opened
 - [90] fix score and lives position
 - **[91]** add cheats
 - [95] speed plats only "wear off" after landing or taking hit
 - [97] fix camera jump at game start
 - [100] add graphical "lanes" for platform lists for debug
 - **[101]** add invincibility powerup
 - **[102]** add extra mobility option to better avoid beam
 - [103] fix player position on game restart
 - **[104]** add beam sfx
 - [105] fix bug where beam launcher damages player while beam is active
 - [106] re-add invincibility timer for collision
 - **[107]** Zoom out camera a bit


## Open Issues ##

 - [0] general game balance tweaks
 - [37] better points sprite
 - [40] zoom out as speed increases
 - **[41]** add graphic for death plane
 - **[42]** different platform paths lean towards different directions
 - [43] increase sense of speed
 - [56] add spawn groups to spawn pattern of objects instead of just within a random range
 - [58] spinning sprite on second jump
 - *[59]* add spikes to bottom of platforms
 - [61] running sfx that scales with speed
 - [71] stop spikes from spawning partly off platforms
 - [74] slow down game pacing and add speed cap
 - *[79]* update sprites to reflect target aesthetic
 - [80] portals to different levels/locations
 - [83] redo spike sprite to better reflect side damage
 - [86] shrink width of player hitbox
 - [92] fix the memory leak
 - [93] improve missile-platform interaction
 - [94] add animation to missile sprite
 - *[96]* allow lateral control while boosted by speed pad
 - [98] fix player lagging behind camera
 - [99] higher pitch sfx for second jump
 - *[107]* add invulnerable music
 - [108] adjust beam launcher height and beam timing so easier to avoid
 - [109] role on landing animation
 - *[110]* let player kill beam launchers while in i-frames
 - *[111]* Dash animation and sfx
 - *[112]* RNG Background platforms
 - **[113]** replace placeholder speed plat asset
 - [114] dashing should still take damage from spikes
 - [115] dashing into beam launcher should give points
 - *[116]* transition layer between backgrounds
 - *[117]* Parallax scrolling 
 - [118] better beam warmup sfx
 

 ## Old ##
 - [45] spawn enemies as function of spawning platform
 - [46] set minimum height separation between platforms
 - [52] add pits to floor
 - [76] spike hitboxes are too big
 - [77] do grid based spawning
 - [84] camera follows player, instead of current player follows camera
 - [85] ~~add varied spawn behavior to different platform paths~~ same as issue42
 
 ## Assets ##
 ### Art ###
  - [x] Player sprite
  - [x] Missile Sprite
  - [ ] Missile Animation
  - [x] Spikes Sprite
  - [ ] Normal Platforms
  - [ ] Speed Platforms
  - [x] Beam Animation
  - [x] Beam Launcher Animation
  - [x] Background
      + [x] caves
      + [x] surface
      + [x] sky 
  - [ ] Death Plane
 
 ### Sound ###
  - [x] bgm
  - [x] Bonk sfx
  - [x] step sfx
  - [x] missile launch sfx
  - [x] beam sfx
  - [x] beam launcher death sfx
  - [x] death sfx
  - [x] hit sfx
  - [x] powerup sfx
  - [ ] invincible music
  - [x] dash sfx


## general ideas ##
can handle platforms differently. Instead of lists of objects that hold their xy position, set game on grid and then track if every cell is a platform, enemy, powerup, etc.

floor has enemy and powerup spawns
player has double jump, but max jump easily hits ceiling
throwable hat
hat is one use
can hold up to 3 (stack on head)
collapsing ceiling that needs to be reset
needs to feel claustrophobic

## notes ##
Don't do grid based platforms, use original linked list. Grid severely limits platforms abilities to reference each other, bad for rng generation outlined in proposal

~~Limit each list of platforms in array to band it can spawn in~~

32x32 pixels seems a good size for a single block size
many objects like spikes, etc will need to be 1x2 at that size
spikes and platforms handled as background tiles, powerups and other enemies handled as sprites

~~camera should fix on player location, with player having a fixed speed rather then player following camera, need to modify how missiles work of go this path
only check collision for enemies if they are on screen~~ This feels weird, do the other thing, fixing player position between 1/2 and 1/5 of screen. Arrow controls should do very little

player now clips though corner of platforms when falling down instead of bonking, updated platform sprite needs to reflect this

to increase screen size, the viewport needs to be expanded over time
this will require modifying when things leave memory dynamically
player left bound will need to be function of viewport size

need dash/hover that gets player past beams
add i-frames?
needs to move cam as well as player so can boundary doesn't prevent dash
hover would avoid cam issue but doesn't help with dodging beams
rotate sprite 90 degrees to show dashing (will look bad but is a necessary placeholder)