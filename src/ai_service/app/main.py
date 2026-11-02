from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from app.detectors.neural_service import NeuralSplicingDetector
import uvicorn
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AI-Microservice")

app = FastAPI(
    title="AI Image Forgery Detection Microservice",
    description="Python FastAPI service providing Deep Learning CNN Splicing Detection & Noise Residual Heatmaps",
    version="1.0.0"
)

# Enable CORS for Spring Boot & Web UI
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

neural_detector = NeuralSplicingDetector()

@app.get("/")
def root():
    return {
        "service": "AI Image Forgery Detection Microservice",
        "status": "ONLINE",
        "endpoints": {
            "health": "/health",
            "analyze": "/api/v1/analyze",
            "docs": "/docs"
        }
    }

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "fastapi-ai-forgery-engine"}

@app.post("/api/v1/analyze")
async def analyze_image(file: UploadFile = File(...)):
    try:
        content_type = file.content_type or ""
        if not (content_type.startswith("image/") or file.filename.lower().endswith((".jpg", ".jpeg", ".png", ".webp"))):
            raise HTTPException(status_code=400, detail="Uploaded file must be a valid image")

        image_bytes = await file.read()
        logger.info(f"Received image: {file.filename} ({len(image_bytes)} bytes)")

        result = neural_detector.analyze(image_bytes)
        return result
    except Exception as e:
        logger.error(f"Analysis failed: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Inference error: {str(e)}")

if __name__ == "__main__":
    uvicorn.run("app.main:app", host="127.0.0.1", port=8000, reload=True)

