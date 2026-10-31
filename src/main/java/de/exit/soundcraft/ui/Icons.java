package de.exit.soundcraft.ui;

/**
 * Hand-drawn pixel icons styled after OpenSoundboard, drawn with fillRect
 * for razor-sharp rendering at any GUI scale.
 */
public enum Icons {

    PLAY(
            "#.....",
            "###...",
            "#####.",
            "######",
            "#####.",
            "###...",
            "#....."),
    PAUSE(
            "##.##",
            "##.##",
            "##.##",
            "##.##",
            "##.##",
            "##.##",
            "##.##"),
    STOP(
            "######",
            "######",
            "######",
            "######",
            "######",
            "######"),
    SKIP_BACK(
            "#....#",
            "#...##",
            "#..###",
            "#.####",
            "#..###",
            "#...##",
            "#....#"),
    SKIP_FORWARD(
            "#....#",
            "##...#",
            "###..#",
            "####.#",
            "###..#",
            "##...#",
            "#....#"),
    SPEAKER(
            "...#...",
            "..##.#.",
            "####..#",
            "####..#",
            "####..#",
            "..##.#.",
            "...#..."),
    SPEAKER_MUTED(
            "...#..#",
            "..##.##",
            "####.#.",
            "####..#",
            "####.#.",
            "..##.##",
            "...#..#"),
    SETTINGS(
            "...#...",
            ".#####.",
            ".#...#.",
            "##.#.##",
            ".#...#.",
            ".#####.",
            "...#..."),
    CLOSE(
            "#.....#",
            ".#...#.",
            "..#.#..",
            "...#...",
            "..#.#..",
            ".#...#.",
            "#.....#"),
    NOTE(
            "...####",
            "...####",
            "...#..#",
            "...#..#",
            ".###.##",
            "#####..",
            ".###..."),
    LYRICS(
            "#######",
            ".......",
            "#####..",
            ".......",
            "######.",
            ".......",
            "###...."),
    REFRESH(
            "..###.#",
            ".#...##",
            "#...###",
            "#......",
            "#.....#",
            ".#...#.",
            "..###..");

    public final int width;
    public final int height;
    private final int[] runs;

    Icons(String... rows) {
        this.height = rows.length;
        int maxWidth = 0;
        int[] buffer = new int[rows.length * 24];
        int count = 0;
        for (int row = 0; row < rows.length; row++) {
            String line = rows[row];
            maxWidth = Math.max(maxWidth, line.length());
            int col = 0;
            while (col < line.length()) {
                if (line.charAt(col) != '#') {
                    col++;
                    continue;
                }
                int start = col;
                while (col < line.length() && line.charAt(col) == '#') col++;
                buffer[count++] = row;
                buffer[count++] = start;
                buffer[count++] = col - start;
            }
        }
        this.width = maxWidth;
        this.runs = java.util.Arrays.copyOf(buffer, count);
    }

    public void draw(UiCanvas c, int x, int y, int argb) {
        for (int i = 0; i < runs.length; i += 3) {
            c.fillRect(x + runs[i + 1], y + runs[i], runs[i + 2], 1, argb);
        }
    }
}
