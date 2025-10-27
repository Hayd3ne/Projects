# embedder.py
import sys
import json
from sentence_transformers import SentenceTransformer

model = SentenceTransformer('all-mpnet-base-v2')

def main(in_path, out_path):
    with open(in_path, "r", encoding="utf-8") as f:
        texts = json.load(f)   # expect a list of strings

    # encode in batch
    embeddings = model.encode(texts, convert_to_numpy=True).tolist()

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(embeddings, f)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python embedder.py input.json output.json")
        sys.exit(1)
    main(sys.argv[1], sys.argv[2])


