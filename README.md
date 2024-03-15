# Proxymaker

Proxymaker has two modes:

### PngMaker
* parses decklists with the following format:
  ```
  #Deck name 1
  1 Muscle Sliver
  1 Winged Sliver
  
  #Deck name 2
  1 Shock
  4 Lightning Bolt
  ```
  **Important!** The number prefix is necessary.
* submits the card names to [PremodernOracle](https://github.com/TheGameKnave/premodernoracle),
by default using an instance at [https://premodernoracle.com](https://premodernoracle.com).
A private instance is preferred due to speed.
* saves 744x1038 px PNG images to a directory
* the example decklist will result in two folders being created in the directory supplied in `properties.yaml`:
    * `Deck name 1` containing `Muscle Sliver.png` and `Winged Sliver.png`
    * `Deck name 2` containing `Shock.png`, `Lighning Bolt.png`, `Lighning Bolt-2.png`, `Lighning Bolt-3.png`, `Lighning Bolt-4.png`
### PdfMaker
* creates an A4 pdf document containing 9 card images on each page
* card images are taken from the supplied directory and all its subdirectories
  * by default, only PNG images are used
* a 0.66 mm bleed is added around the outer edges of the 3x3 card image grid
    * available colors: `BLACK`, `WHITE`, `GOLD`, `SILVER`
* each card image is exactly 63x88 mm
* cutting guides are added

### Configuration
Edit the `properties.yaml` file.
