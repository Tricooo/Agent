from pathlib import Path


BASE_DIR = Path(__file__).resolve().parents[1]
RAG_EVAL_DIR = BASE_DIR.parent
RESULTS_DIR = RAG_EVAL_DIR / "results"
INTERNAL_CASES_PATH = RAG_EVAL_DIR / "cases.json"
EXTERNAL_CASES_DIR = RAG_EVAL_DIR / "external-samples" / "cases"
STATIC_DIR = BASE_DIR / "static"
DATA_DIR = BASE_DIR / ".workbench"
DB_PATH = DATA_DIR / "rag_eval_workbench.sqlite"
