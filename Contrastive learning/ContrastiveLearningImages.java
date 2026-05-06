import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ContrastiveLearningImages {

    /**
     * [新增功能] 自動生成標準的 JPEG 測試圖片，徹底解決網路圖片格式不相容 (如 WebP) 的問題！
     */
    public static void generateStandardTestImages() {
        try {
            // 生成 Cat 1 (淺灰底，黑圓形)
            createDummyImage("cat1.jpg", Color.LIGHT_GRAY, Color.BLACK, "CIRCLE");
            // 生成 Cat 2 (淺灰底，深灰圓形 -> 特徵與 Cat 1 相似)
            createDummyImage("cat2.jpg", Color.LIGHT_GRAY, Color.DARK_GRAY, "CIRCLE");
            // 生成 Dog (橘底，白方形 -> 特徵與 Cat 完全不同)
            createDummyImage("dog.jpg", Color.ORANGE, Color.WHITE, "SQUARE");
            System.out.println("✅ [系統初始化] 成功生成標準的測試圖片 (cat1.jpg, cat2.jpg, dog.jpg)！\n");
        } catch (Exception e) {
            System.out.println("❌ 圖片生成失敗：" + e.getMessage());
        }
    }

    private static void createDummyImage(String fileName, Color bg, Color shape, String type) throws Exception {
        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        // 畫背景
        g.setColor(bg);
        g.fillRect(0, 0, size, size);
        
        // 畫特徵圖形
        g.setColor(shape);
        if ("CIRCLE".equals(type)) {
            g.fillOval(16, 16, 32, 32);
        } else {
            g.fillRect(16, 16, 32, 32);
        }
        g.dispose();
        
        // 強制輸出最標準的 JPG 格式
        ImageIO.write(img, "jpg", new File(fileName));
    }

    /**
     * 模擬神經網路的特徵萃取函數 D(x)
     */
    public static double[] extractFeatures(String imagePath) throws Exception {
        BufferedImage img = ImageIO.read(new File(imagePath));
        if (img == null) {
            throw new Exception("找不到圖片或格式無法解析: " + imagePath);
        }

        int size = 8;
        BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();

        double[] features = new double[size * size];
        int index = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                features[index++] = (resized.getRGB(x, y) & 0xFF) / 255.0;
            }
        }
        return features;
    }

    /**
     * 計算兩個特徵向量之間的 L2 距離 (Euclidean Distance)
     */
    public static double calculateL2Distance(double[] v1, double[] v2) {
        double sumSq = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sumSq += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sumSq);
    }

    /**
     * 計算負樣本的對比學習距離損失 (Hinge Loss)
     */
    public static double calculateNegativeSampleLoss(double distance, double marginC) {
        return Math.max(0, marginC - distance);
    }

    public static void main(String[] args) {
        try {
            System.out.println("==========================================================");
            System.out.println("   Assignment 1: Contrastive Learning (Negative Samples)");
            System.out.println("   Loss 公式: L = max(0, C - ||D(x1) - D(x2)||_2)        ");
            System.out.println("==========================================================");
            
            // 1. 自動覆蓋/生成正確格式的圖片 (解決讀檔 null 問題)
            generateStandardTestImages();
            
            // 2. 讀取特徵
            double[] featureCat1 = extractFeatures("cat1.jpg");
            double[] featureCat2 = extractFeatures("cat2.jpg");
            double[] featureDog = extractFeatures("dog.jpg");

            // 3. 設定常數 C (Margin)
            double marginC = 3.0; 
            System.out.println("[全局參數] 設定 Margin (C) = " + marginC + "\n");

            // --------------------------------------------------------
            // 負樣本 1: Cat 1 vs Dog
            // --------------------------------------------------------
            System.out.println("----------------------------------------------------------");
            System.out.println("【測試一】負樣本對 (Negative Pair) : Cat 1 與 Dog");
            System.out.println("   -> 目標：不同類別的圖片，特徵距離應該要大於 Margin (C)。");
            
            double dist1 = calculateL2Distance(featureCat1, featureDog);
            double loss1 = calculateNegativeSampleLoss(dist1, marginC);
            
            System.out.printf("   ▶ 步驟 1: L2 距離 ||D(x1) - D(x2)||_2 = %.4f\n", dist1);
            System.out.printf("   ▶ 步驟 2: 代入公式 Loss = max(0, %.1f - %.4f)\n", marginC, dist1);
            System.out.printf("   ▶ 最終 Loss 結果 = %.4f\n", loss1);
            
            if(loss1 > 0) {
                System.out.println("   [結果分析] 距離小於 " + marginC + "！產生了 Loss，神經網路需要優化把牠們推開。");
            } else {
                System.out.println("   [結果分析] 距離已經大於 " + marginC + "，非常安全！Loss 為 0。");
            }
            
            // --------------------------------------------------------
            // 負樣本 2: Cat 2 vs Dog
            // --------------------------------------------------------
            System.out.println("----------------------------------------------------------");
            System.out.println("【測試二】負樣本對 (Negative Pair) : Cat 2 與 Dog");
            System.out.println("   -> 目標：不同類別的圖片，特徵距離應該要大於 Margin (C)。");
            
            double dist2 = calculateL2Distance(featureCat2, featureDog);
            double loss2 = calculateNegativeSampleLoss(dist2, marginC);
            
            System.out.printf("   ▶ 步驟 1: L2 距離 ||D(x1) - D(x2)||_2 = %.4f\n", dist2);
            System.out.printf("   ▶ 步驟 2: 代入公式 Loss = max(0, %.1f - %.4f)\n", marginC, dist2);
            System.out.printf("   ▶ 最終 Loss 結果 = %.4f\n", loss2);
            
            if(loss2 > 0) {
                System.out.println("   [結果分析] 距離小於 " + marginC + "！產生了 Loss，神經網路需要優化把牠們推開。");
            } else {
                System.out.println("   [結果分析] 距離已經大於 " + marginC + "，非常安全！Loss 為 0。");
            }

            // --------------------------------------------------------
            // 正樣本對 (作為對照組)
            // --------------------------------------------------------
            System.out.println("----------------------------------------------------------");
            System.out.println("【對照組】正樣本對 (Positive Pair) : Cat 1 與 Cat 2");
            System.out.println("   -> 目標：同類別的圖片，特徵距離應該越小越好。");
            
            double distPos = calculateL2Distance(featureCat1, featureCat2);
            
            System.out.printf("   ▶ 步驟 1: L2 距離 ||D(x1) - D(x2)||_2 = %.4f\n", distPos);
            System.out.println("   [結果分析] 正樣本的目標是拉近距離，因此不套用 max(0, C - Distance) 這個負樣本公式。");
            System.out.println("==========================================================");

        } catch (Exception e) {
            System.out.println("發生非預期錯誤：");
            e.printStackTrace();
        }
    }
}